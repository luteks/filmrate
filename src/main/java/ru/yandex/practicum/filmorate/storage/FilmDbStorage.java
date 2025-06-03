package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final JdbcTemplate jdbcTemplate;

    private static Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder().id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .mpa(new Mpa(resultSet.getInt("rating_id"), resultSet.getString("mpa_rating.name")))
                .directors(new HashSet<>())
                .likes(new HashSet<>())
                .build();
    }

    private void setFilmGenres(Film film) {
        String sql = "DELETE FROM film_genres WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource("film_id", film.getId()));

        sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (:film_id, :genre_id);";
        SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(film.getGenres().stream().map(genre -> {
            Map<String, Object> par = new HashMap<>();
            par.put("film_id", film.getId());
            par.put("genre_id", genre.getId());
            return par;
        }).toList());
        jdbc.batchUpdate(sql, batchParams);
    }

    private void setFilmDirectors(Film film) {
        final String DELETE_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id=?";
        jdbcTemplate.update(DELETE_DIRECTORS_QUERY, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            final String ADD_DIRECTORS_QUERY = "INSERT INTO film_directors (film_id, director_id) VALUES(?, ?)";
            jdbcTemplate.batchUpdate(ADD_DIRECTORS_QUERY, film.getDirectors().stream().map(director -> new Object[]{film.getId(), director.getId()}).toList());
        }
    }

    private void loadDirectorsToFilm(Film film) {
        final String FIND_FILM_DIRECTORS = "SELECT d.director_id, d.name " + "FROM film_directors AS fd " + "JOIN directors AS d ON fd.director_id = d.director_id " + "WHERE fd.film_id=?";
        Set<Director> directors = new HashSet<>(jdbcTemplate.query(FIND_FILM_DIRECTORS, (rs, rowNum) -> new Director(rs.getInt("director_id"), rs.getString("name")), film.getId()));
        film.setDirectors(directors);
    }

    private void loadLikesToFilm(Film film) {
        final String FIND_FILM_LIKES = "SELECT user_id FROM likes WHERE film_id=?";
        Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(FIND_FILM_LIKES, Long.class, film.getId()));
        film.setLikes(likes);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films " + "(name, description, release_date, duration, rating_id)" + "VALUES (:name, :description, :release_date, :duration, :rating_id);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(film.toMap()), keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));
        setFilmGenres(film);
        setFilmDirectors(film);
        loadDirectorsToFilm(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = :name, description = :description, " + "release_date = :release_date, duration = :duration, rating_id = :rating_id " + "WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource(newFilm.toMap()));
        setFilmGenres(newFilm);
        setFilmDirectors(newFilm);
        loadDirectorsToFilm(newFilm);
        loadLikesToFilm(newFilm);
        return newFilm;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " + "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "ORDER BY f.film_id";

        return jdbc.query(sql, rs -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (rs.next()) {
                if (film == null || !film.getId().equals(rs.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
                }
            }
            if (film != null) {
                films.add(film);
            }
            films.forEach(this::loadDirectorsToFilm);
            return films;
        });
    }

    @Override
    public Film findById(Long filmId) {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " + "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "WHERE f.film_id = :film_id;";
        return jdbc.query(sql, new MapSqlParameterSource("film_id", filmId), rs -> {
            Film film = null;
            while (rs.next()) {
                if (film == null) {
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
            }
            loadDirectorsToFilm(film);
            return film;
        });
    }

    @Override
    public void deleteFilmById(Long filmId) {
        String sql = "DELETE FROM films WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource("film_id", filmId));
    }

    @Override
    public void addLikeByUser(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (user_id, film_id) VALUES (:user_id, :film_id);";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("film_id", filmId);

        jdbc.update(sql, parameterSource);
    }

    @Override
    public Collection<Film> getTopFilms(Integer count, Integer genId, Integer year) {
        String sql = """
                SELECT f.*,
                       mpa.name AS mpa_rating_name,
                       g.genre_id,
                       g.name AS genre_name,
                       COUNT(l.user_id) AS likes_count
                FROM films f
                LEFT JOIN mpa_rating mpa ON f.rating_id = mpa.rating_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN likes l ON f.film_id = l.film_id
                WHERE (:genreId IS NULL OR fg.genre_id = :genreId)
                  AND (:year IS NULL OR EXTRACT(YEAR FROM f.release_date) = :year)
                GROUP BY f.film_id, mpa.rating_id, g.genre_id
                ORDER BY likes_count DESC
                LIMIT :count
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count)
                .addValue("genreId", genId)
                .addValue("year", year);

        return jdbc.query(sql, params, rs -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (rs.next()) {
                if (film == null || !film.getId().equals(rs.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
                }
            }
            if (film != null) {
                loadDirectorsToFilm(film);
                films.add(film);
            }
            return films;
        });
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT
                    f.film_id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    f.rating_id,
                    r.name AS rating_name,
                    g.genre_id,
                    g.name AS genre_name,
                    COUNT(l.user_id) AS likes_count
                FROM films f
                JOIN likes l ON f.film_id = l.film_id
                JOIN mpa_rating r ON f.rating_id = r.rating_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                WHERE f.film_id IN (
                    SELECT film_id FROM likes WHERE user_id = :user_id
                    INTERSECT
                    SELECT film_id FROM likes WHERE user_id = :friend_id
                )
                GROUP BY f.film_id, r.name, g.genre_id, g.name
                ORDER BY likes_count DESC;
                """;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("friend_id", friendId);

        return jdbc.query(sql, parameterSource, rs -> {
            Collection<Film> films = new LinkedList<>();
            Film film = null;
            while (rs.next()) {
                if (film == null || !film.getId().equals(rs.getLong("film_id"))) {
                    if (film != null) {
                        films.add(film);
                    }
                    film = mapRowToFilm(rs, rs.getRow());
                }
                Integer genreId = rs.getInt("genre_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
                }
            }
            if (film != null) {
                films.add(film);
            }
            return films;
        });
    }

    public Collection<Film> getFilmsByDirectorId(int directorId) {
        final String FIND_FILMS_BY_DIRECTOR = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " + "FROM films AS f " + "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " + "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " + "LEFT JOIN genres ON fg.genre_id = genres.genre_id " + "LEFT JOIN film_directors AS fd ON fd.film_id = f.film_id " + "WHERE fd.director_id=?";

        Collection<Film> films = jdbcTemplate.query(FIND_FILMS_BY_DIRECTOR, FilmDbStorage::mapRowToFilm, directorId);
        films.forEach(film -> {
            loadLikesToFilm(film);
            loadDirectorsToFilm(film);
        });
        return films;
    }

    @Override
    public Collection<Film> getLikedFilms(long userId) {
        final String FIND_LIKED_FILMS_BY_USER_ID = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " + "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
                "LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres ON fg.genre_id = genres.genre_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "WHERE l.user_id=?";
        List<Film> likedFilms = jdbcTemplate.query(FIND_LIKED_FILMS_BY_USER_ID, FilmDbStorage::mapRowToFilm, userId);
        likedFilms.forEach(film -> {
            loadLikesToFilm(film);
            loadDirectorsToFilm(film);
        });
        return likedFilms;
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE user_id = :user_id AND film_id = :film_id;";

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("film_id", filmId);
        jdbc.update(sql, parameterSource);
    }

    @Override
    public boolean isFilmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = :film_id;";
        return 1 == jdbc.queryForObject(sql, new MapSqlParameterSource("film_id", filmId), Integer.class);
    }

    @Override
    public List<Film> searchByTitle(String query) {
        String sql = """
        SELECT DISTINCT f.*, mpa.name AS mpa_rating_name
        FROM films f
        LEFT JOIN mpa_rating mpa ON f.rating_id = mpa.rating_id
        WHERE LOWER(f.name) LIKE LOWER(?)
        """;

        List<Film> films = jdbcTemplate.query(
                sql,
                FilmDbStorage::mapRowToFilm,
                "%" + query.toLowerCase() + "%"
        );

        loadFilmData(films);
        return films;
    }

    @Override
    public List<Film> searchByDirector(String query) {
        String sql = """
        SELECT DISTINCT f.*, mpa.name AS mpa_rating_name
        FROM films f
        LEFT JOIN mpa_rating mpa ON f.rating_id = mpa.rating_id
        WHERE f.film_id IN (
            SELECT fd.film_id
            FROM film_directors fd
            JOIN directors d ON fd.director_id = d.director_id
            WHERE LOWER(d.name) LIKE LOWER(?)
        )
        """;

        List<Film> films = jdbcTemplate.query(
                sql,
                FilmDbStorage::mapRowToFilm,
                "%" + query.toLowerCase() + "%"
        );

        loadFilmData(films);
        return films;
    }

    @Override
    public List<Film> searchByBoth(String query) {
        String sql = """
        SELECT DISTINCT f.*, mpa.name AS mpa_rating_name
        FROM films f
        LEFT JOIN mpa_rating mpa ON f.rating_id = mpa.rating_id
        WHERE LOWER(f.name) LIKE LOWER(?)
        OR f.film_id IN (
            SELECT fd.film_id
            FROM film_directors fd
            JOIN directors d ON fd.director_id = d.director_id
            WHERE LOWER(d.name) LIKE LOWER(?)
        )
        """;

        String searchPattern = "%" + query.toLowerCase() + "%";
        List<Film> films = jdbcTemplate.query(
                sql,
                FilmDbStorage::mapRowToFilm,
                searchPattern, searchPattern
        );

        loadFilmData(films);
        return films;
    }

    private void loadFilmData(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        if (!filmMap.isEmpty()) {
            String directorsSql = """
            SELECT fd.film_id, d.director_id, d.name
            FROM film_directors fd
            JOIN directors d ON fd.director_id = d.director_id
            WHERE fd.film_id IN (%s)
            """.formatted(
                    filmMap.keySet().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))
            );

            jdbcTemplate.query(directorsSql, (rs) -> {
                Film film = filmMap.get(rs.getLong("film_id"));
                if (film != null) {
                    film.getDirectors().add(new Director(
                            rs.getInt("director_id"),
                            rs.getString("name")
                    ));
                }
            });
        }

        if (!filmMap.isEmpty()) {
            String likesSql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" +
                    filmMap.keySet().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")) + ")";

            jdbcTemplate.query(likesSql, (rs) -> {
                Film film = filmMap.get(rs.getLong("film_id"));
                if (film != null) {
                    film.getLikes().add(rs.getLong("user_id"));
                }
            });
        }

        if (!filmMap.isEmpty()) {
            String genresSql = """
            SELECT fg.film_id, g.genre_id, g.name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.genre_id
            WHERE fg.film_id IN (%s)
            """.formatted(
                    filmMap.keySet().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))
            );

            jdbcTemplate.query(genresSql, rs -> {
                Film film = filmMap.get(rs.getLong("film_id"));
                if (film != null) {
                    film.getGenres().add(new Genre(
                            rs.getInt("genre_id"),
                            rs.getString("name")
                    ));
                }
            });
        }

        films.sort((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()));
    }
}