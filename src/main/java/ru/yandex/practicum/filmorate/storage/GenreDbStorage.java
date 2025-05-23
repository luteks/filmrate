package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
@Primary
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final NamedParameterJdbcOperations jdbc;

    private Genre mapRowToGenre(ResultSet resultSet, int rawNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public Genre getGenreById(Integer genreId) {
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
    public boolean isGenreExists(Integer genreId) {
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE genre_id = :genre_id;";

        return 1 == jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("genre_id", genreId),
                Integer.class);
    }

    @Override
    public boolean areGenresExist(List<Integer> genreId) {
        int genresIdsSize = genreId.size();
        if (genresIdsSize == 0) return true;
        String sqlQuery = "SELECT COUNT(*) FROM genres WHERE genre_id IN (:genre_id);";
        return genresIdsSize ==
                jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("genre_id", genreId), Integer.class);
    }
}