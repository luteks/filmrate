package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getInt("director_id"))
                .name(rs.getString("name"))
                .build();
    }

    @Override
    public Director create(Director director) {
        final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        int id = keyHolder.getKey().intValue();
        director.setId(id);

        return director;
    }

    @Override
    public Collection<Director> findAll() {
        final String FIND_ALL_QUERY = "SELECT director_id, name FROM directors";
        return jdbcTemplate.query(FIND_ALL_QUERY, this::mapRowToDirector);
    }

    @Override
    public Optional<Director> findById(int id) {
        final String FIND_BY_ID_QUERY = "SELECT director_id, name FROM directors WHERE director_id=?";
        Director director;
        try {
            director = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, this::mapRowToDirector, id);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
        return Optional.ofNullable(director);
    }

    @Override
    public Director update(Director director) {
        final String UPDATE_QUERY = "UPDATE directors SET name=?";
        jdbcTemplate.update(UPDATE_QUERY, director.getName());
        return director;
    }

    @Override
    public void deleteById(int id) {
        final String DELETE_BY_ID_QUERY = "DELETE FROM directors WHERE director_id=?";
        int deletedRows = jdbcTemplate.update(DELETE_BY_ID_QUERY, id);

        if (deletedRows == 0) {
            throw new NotFoundException("режиссер с id " + id + " не найден");
        }
    }
}
