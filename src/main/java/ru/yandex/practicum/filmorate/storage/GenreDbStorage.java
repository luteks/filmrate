package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final JdbcTemplate jdbcTemplate;

    private Genre mapRowToGenre(ResultSet resultSet, int rawNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getLong("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public Genre getGenreById(Long genreId) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id = :genre_id;";

        return jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("genre_id", genreId),
                this::mapRowToGenre);
    }

    @Override
    public Collection<Genre> findAll() {
        String sqlQuery = "SELECT * FROM genres ORDER BY genre_id;";
        return jdbc.query(sqlQuery, this::mapRowToGenre);
    }

    @Override
    public boolean isGenreExists(Long genreId) {
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE genre_id = :genre_id;";

        Integer count = jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("genre_id", genreId),
                Integer.class);

        return count != null && count == 1;
    }

    @Override
    public boolean areGenresExist(List<Long> genreId) {
        if (genreId == null || genreId.isEmpty()) {
            return true;
        }
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE genre_id IN (:genre_id);";

        Integer count = jdbc.queryForObject(sqlQuery,
                new MapSqlParameterSource("genre_id", genreId), Integer.class);

        return count != null && count == genreId.size();
    }

    @Override
    public void delete(Long id) {
        deleteRelated(Optional.of(id));
        String sql = "DELETE FROM genres WHERE genre_id = :genre_id;";
        jdbc.update(sql, new MapSqlParameterSource("genre_id", id));
    }

    @Override
    public void deleteAll() {
        deleteRelated(Optional.empty());
        String sql = "DELETE FROM genres";
        jdbcTemplate.update(sql);
    }

    private void deleteRelated(Optional<Long> genreId) {
        final String DELETE_FILM_GENRE = "DELETE FROM film_genres";
        final String DELETE_FILM_GENRE_BY_GENRE_ID = "DELETE FROM film_genres WHERE genre_id = :genre_id;";

        if (genreId.isPresent()) {
            MapSqlParameterSource param = new MapSqlParameterSource("genre_id", genreId.get());
            jdbc.update(DELETE_FILM_GENRE_BY_GENRE_ID, param);
        } else {
            jdbcTemplate.update(DELETE_FILM_GENRE);
        }
    }
}