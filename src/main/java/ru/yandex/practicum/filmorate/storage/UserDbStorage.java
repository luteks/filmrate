package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final NamedParameterJdbcOperations jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("user_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(LocalDate.parse(resultSet.getString("birthday")))
                .build();
    }

    @Override
    public User create(User user) {
        String sqlQuery = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (:email, :login, :name, :birthday);";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sqlQuery, new MapSqlParameterSource(user.toMap()), keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = :user_id;";
        return jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("user_id", userId), this::mapRowToUser);
    }

    @Override
    public Collection<User> getUsers() {
        String sqlQuery = "SELECT * FROM users;";
        return jdbc.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User update(User user) {
        String sqlQuery = "UPDATE users SET email = :email, login = :login, name = :name," +
                " birthday = :birthday WHERE user_id = :user_id;";
        jdbc.update(sqlQuery, user.toMap());
        return user;
    }

    @Override
    public void delete(Long userId) {
        deleteRelated(Optional.of(userId));
        String sqlQuery = "DELETE FROM users WHERE user_id = :user_id;";
        jdbc.update(sqlQuery, new MapSqlParameterSource("user_id", userId));
    }

    @Override
    public void deleteAll() {
        deleteRelated(Optional.empty());
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }

    private void deleteRelated(Optional<Long> userId) {
        final String DELETE_ALL_FRIENDS = "DELETE FROM friendship";
        final String DELETE_LIKE_BY_ID = "DELETE FROM likes WHERE user_id = :user_id;";
        final String DELETE_ALL_LIKES = "DELETE FROM likes";
        final String DELETE_ALL_FRIENDS_BY_USER_ID = "DELETE FROM friendship WHERE user_id = :user_id;";

        if (userId.isPresent()) {
            MapSqlParameterSource param = new MapSqlParameterSource("user_id", userId.get());
            jdbc.update(DELETE_LIKE_BY_ID, param);
            jdbc.update(DELETE_ALL_FRIENDS_BY_USER_ID, param);
        } else {
            jdbcTemplate.update(DELETE_ALL_LIKES);
            jdbcTemplate.update(DELETE_ALL_FRIENDS);
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sqlQuery = "INSERT INTO friendship (user_id, friend_id) VALUES (:user_id, :friend_id);";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("friend_id", friendId);

        jdbc.update(sqlQuery, params);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id IN " +
                "(SELECT friend_id FROM friendship WHERE user_id = :user_id);";

        return jdbc.query(sqlQuery, new MapSqlParameterSource("user_id", userId), this::mapRowToUser);
    }

    @Override
    public Collection<User> getCommonFriends(Long firstUserId, Long secondUserId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id IN " +
                "(SELECT friend_id FROM friendship WHERE user_id = :first_user_id " +
                "INTERSECT " +
                "SELECT friend_id FROM friendship WHERE user_id = :second_user_id);";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("first_user_id", firstUserId);
        params.addValue("second_user_id", secondUserId);
        return jdbc.query(sqlQuery, params, this::mapRowToUser);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sqlQuery = "DELETE FROM friendship WHERE user_id = :user_id AND friend_id = :friend_id;";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user_id", userId);
        params.addValue("friend_id", friendId);

        jdbc.update(sqlQuery, params);
    }

    @Override
    public boolean isUserExistsWithEmail(User user) {
        String sqlQuery = "SELECT COUNT(*) FROM users WHERE email = :email" +
                (user.getId() == null ? "" : " AND user_id <> :user_id") + ";";
        return 1 == jdbc.queryForObject(sqlQuery, new MapSqlParameterSource(user.toMap()), Integer.class);
    }

    @Override
    public boolean isUserExists(Long userId) {
        String sqlQuery = "SELECT COUNT(*) FROM users WHERE user_id = :user_id;";

        return 1 == jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("user_id", userId), Integer.class);
    }

    public List<User> findSimilarUsers(Long userId) {
        final String FIND_COMMON_LIKES_USERS = """
                SELECT u.user_id
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                JOIN users u ON l2.user_id = u.user_id
                WHERE l1.user_id = :user_id
                  AND u.user_id != :user_id
                GROUP BY u.user_id
                ORDER BY COUNT(l2.film_id) DESC
                FETCH FIRST 10 ROWS ONLY
                """.stripIndent();

        return jdbc.query(FIND_COMMON_LIKES_USERS, new MapSqlParameterSource("user_id", userId), this::mapRowToUser);
    }

    @Override
    public List<Film> findRecommendedFilmsForUser(Long userId) {
        List<User> similarUsers = findSimilarUsers(userId);

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        Long similarUserId = similarUsers.getFirst().getId();

        return filmStorage.findRecommendations(similarUserId, userId);
    }
}