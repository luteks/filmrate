package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final NamedParameterJdbcOperations jdbc;

    private static Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(LocalDate.parse(resultSet.getString("release_date")))
                .duration(resultSet.getInt("duration"))
                .mpa(new Mpa(resultSet.getInt("rating_id"), resultSet.getString("mpa_rating.name")))
                .build();
    }

    private void setFilmGenres(Film film) {
        String sql = "DELETE FROM film_genres WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource("film_id", film.getId()));

        sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (:film_id, :genre_id);";
        SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(
                film.getGenres().stream().map(genre -> {
                    Map<String, Object> par = new HashMap<>();
                    par.put("film_id", film.getId());
                    par.put("genre_id", genre.getId());
                    return par;
                }).toList()
        );
        jdbc.batchUpdate(sql, batchParams);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films " +
                "(name, description, release_date, duration, rating_id)" +
                "VALUES (:name, :description, :release_date, :duration, :rating_id);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(film.toMap()), keyHolder);
        film.setId(keyHolder.getKeyAs(Long.class));
        setFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String sql = "UPDATE films SET name = :name, description = :description, " +
                "release_date = :release_date, duration = :duration, rating_id = :rating_id " +
                "WHERE film_id = :film_id;";
        jdbc.update(sql, new MapSqlParameterSource(newFilm.toMap()));
        setFilmGenres(newFilm);
        return newFilm;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
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
            return films;
        });
    }

    @Override
    public Film findById(Long filmId) {
        String sql = "SELECT f.*, mpa.name AS mpa_rating_name, genres.genre_id, genres.name AS genre_name " +
                "FROM films AS f " +
                "LEFT JOIN mpa_rating AS mpa ON f.rating_id = mpa.rating_id " +
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
                if (!rs.wasNull())
                    film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
            }
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
                films.add(film);
            }
            return films;
        });
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
}