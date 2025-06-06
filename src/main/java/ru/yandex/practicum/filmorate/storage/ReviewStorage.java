package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Review findById(Long id);

    List<Review> getByFilmId(Long filmId);

    List<Review> getAll();

    boolean hasUserRatedTheReview(Long reviewId, Long userId);

    boolean getUserRating(Long reviewId, Long userId);

    void addLike(Long reviewId, Long userId);

    void addDislike(Long reviewId, Long userId);

    void deleteRating(Long reviewId, Long userId);

    void updateRating(Review review);

    boolean isReviewExist(Long id);
}
