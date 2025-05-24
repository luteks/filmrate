package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final NamedParameterJdbcOperations jdbc;

    private Mpa mapRowToMpa(ResultSet resultSet, int rowNum) throws SQLException {
        return Mpa.builder()
                .id(resultSet.getInt("rating_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    @Override
    public boolean isMpaExists(Integer mpaId) {
        String sqlQuery = "SELECT COUNT(*) FROM mpa_rating WHERE rating_id = :rating_id;";
        return 1 == jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("rating_id", mpaId), Integer.class);
    }

    @Override
    public Mpa getMpaById(Integer mpaId) {
        String sqlQuery = "SELECT * FROM mpa_rating WHERE rating_id = :rating_id;";
        return jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("rating_id", mpaId), this::mapRowToMpa);
    }

    @Override
    public Collection<Mpa> findAll() {
        String sqlQuery = "SELECT * FROM mpa_rating ORDER BY rating_id;";
        return jdbc.query(sqlQuery, this::mapRowToMpa);
    }
}