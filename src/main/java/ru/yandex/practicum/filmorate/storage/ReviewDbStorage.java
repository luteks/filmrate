package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

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
                "content = ?, is_positive=?, user_id =?, film_id =? WHERE review_id=?";
        jdbcTemplate.update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getUserId(), review.getFilmId(), review.getReviewId());
        return review;
    }

    @Override
    public Collection<Review> findReviewsByFilmId(Long filmId, int limit) {
        final String REVIEWS_OF_FILM = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews " +
                "WHERE film_id=? " +
                "LIMIT(?)";
        try {
            return jdbcTemplate.query(REVIEWS_OF_FILM, this::mapRowToReview, filmId, limit);
        } catch (EmptyResultDataAccessException ex) {
            return Collections.emptyList();
        }

    }

    @Override
    public Optional<Review> findById(Long id) {
        final String FIND_REVIEW_QUERY = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews " +
                "WHERE review_id=?";
        Review review;
        try {
            review = jdbcTemplate.queryForObject(FIND_REVIEW_QUERY, this::mapRowToReview, id);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
        return Optional.ofNullable(review);
    }

    @Override
    public Collection<Review> findAll(int limit) {
        final String FIND_ALL_QUERY = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews LIMIT(?)";
        try {
            return jdbcTemplate.query(FIND_ALL_QUERY, this::mapRowToReview, limit);
        } catch (EmptyResultDataAccessException ex) {
            return Collections.emptyList();
        }

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
