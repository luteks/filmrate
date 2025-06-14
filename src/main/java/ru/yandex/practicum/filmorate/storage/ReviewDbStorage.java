package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcOperations jdbc;

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .reviewId(rs.getLong("review_id"))
                .content(rs.getString("content"))
                .isPositive(rs.getBoolean("is_positive"))
                .userId(rs.getLong("user_id"))
                .filmId(rs.getLong("film_id"))
                .useful(rs.getInt("useful"))
                .build();
    }

    @Override
    public Review update(Review review) {
        final String UPDATE_QUERY = "UPDATE reviews SET " +
                "content = ?, is_positive=? WHERE review_id=?";
        jdbcTemplate.update(UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        return findById(review.getReviewId());
    }

    @Override
    public List<Review> getAll() {
        final String GET_ALL = """
                    SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews
                    """;
        return jdbcTemplate.query(GET_ALL, this::mapRowToReview);
    }

    @Override
    public List<Review> getByFilmId(Long filmId) {
        final String GET_BY_FILM_ID = """
                    SELECT review_id, content, is_positive, user_id, film_id, useful
                    FROM reviews WHERE film_id = ?
                    """;
        return jdbcTemplate.query(GET_BY_FILM_ID, this::mapRowToReview, filmId);
    }

    @Override
    public Review findById(Long id) {
        final String FIND_REVIEW_QUERY = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews " +
                "WHERE review_id=?";
        Review review = jdbcTemplate.queryForObject(FIND_REVIEW_QUERY, this::mapRowToReview, id);
        return review;
    }

    @Override
    public boolean isReviewExist(Long id) {
        String sqlQuery = "SELECT COUNT(*) FROM reviews WHERE review_id = :review_id;";

        Long count = jdbc.queryForObject(sqlQuery, new MapSqlParameterSource("review_id", id), Long.class);

        return count != null && count == 1;
    }

    @Override
    public void delete(Long id) {
        final String DELETE_QUERY = "DELETE FROM reviews WHERE review_id=?";
        jdbcTemplate.update(DELETE_QUERY, id);
    }

    @Override
    public Review create(Review review) {
        final String CREATE_QUERY = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES(?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, review.getContent());
            preparedStatement.setBoolean(2, review.getIsPositive());
            preparedStatement.setLong(3, review.getUserId());
            preparedStatement.setLong(4, review.getFilmId());
            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        review.setReviewId(id);

        return review;
    }

    @Override
    public boolean getUserRating(Long reviewId, Long userId) {
        final String FIND_RATE_QUERY = "SELECT rating FROM review_ratings WHERE review_id=? AND user_id=?";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(FIND_RATE_QUERY, Boolean.class, reviewId, userId));
    }

    @Override
    public boolean hasUserRatedTheReview(Long reviewId, Long userId) {
        final String FIND_RATE_QUERY = "SELECT id FROM review_ratings WHERE review_id=? AND user_id=?";
        try {
            jdbcTemplate.queryForObject(FIND_RATE_QUERY, Integer.class, reviewId, userId);
        } catch (EmptyResultDataAccessException ex) {
            return false;
        }
        return true;
    }

    @Override
    public void deleteRating(Long reviewId, Long userId) {
        final String DELETE_QUERY = "DELETE FROM review_ratings WHERE review_id=? AND user_id=?";
        jdbcTemplate.update(DELETE_QUERY, reviewId, userId);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        final String ADD_LIKE_QUERY = "INSERT INTO review_ratings (user_id, review_id, rating) VALUES (?, ?, TRUE)";
        jdbcTemplate.update(ADD_LIKE_QUERY, userId, reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        final String ADD_DISLIKE_QUERY = "INSERT INTO review_ratings (user_id, review_id, rating) VALUES (?, ?, FALSE)";
        jdbcTemplate.update(ADD_DISLIKE_QUERY, userId, reviewId);
    }

    @Override
    public void updateRating(Review review) {
        final String UPDATE_RATING_QUERY = "UPDATE reviews SET useful=? WHERE review_id=?";
        jdbcTemplate.update(UPDATE_RATING_QUERY, review.getUseful(), review.getReviewId());
    }
}
