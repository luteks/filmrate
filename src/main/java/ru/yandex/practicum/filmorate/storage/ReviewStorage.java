package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(int id);

    Optional<Review> findById(int id);

    Collection<Review> findAll(int limit);

    Collection<Review> findReviewsByFilmId(Long filmId, int limit);

    boolean hasUserRatedTheReview(Integer reviewId, Long userId);

    boolean getUserRating(Integer reviewId, Long userId);

    void addLike(Integer reviewId, Long userId);

    void addDislike(Integer reviewId, Long userId);

    void deleteRating(Integer reviewId, Long userId);

    void updateRating(Review review);
}
