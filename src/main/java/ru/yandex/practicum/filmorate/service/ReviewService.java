package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review findById(int id) {
        return getReviewById(id);
    }

    public Collection<Review> findAll(int limit) {
        return reviewStorage.findAll(limit);
    }

    public Collection<Review> findReviewsOfFilm(Long filmId, int limit) {
        return reviewStorage.findReviewsByFilmId(filmId, limit);
    }

    public Review create(Review review) {
        validateFilm(review.getFilmId());
        validateUser(review.getUserId());
        log.debug("создан новый отзыв{}", review);
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new IllegalArgumentException("отсутствует id");
        }
        validateFilm(review.getFilmId());
        validateUser(review.getUserId());
        log.debug("отзыв обновлен{}", review);
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public void addLike(Integer reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            throw new IllegalArgumentException("пользователь " + userId + " уже ставил лайк отзыву " + reviewId);
        } else if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            review.removeDislike();
            reviewStorage.deleteRating(reviewId, userId);
        }
        reviewStorage.addLike(reviewId, userId);
        review.addLike();
        reviewStorage.updateRating(review);
    }

    public void addDislike(Integer reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            throw new IllegalArgumentException("пользователь " + userId + " уже ставил дизлайк отзыву " + reviewId);
        } else if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            review.removeLike();
            reviewStorage.deleteRating(reviewId, userId);
        }
        reviewStorage.addDislike(reviewId, userId);
        review.addDislike();
        reviewStorage.updateRating(review);
    }

    public void deleteLike(Integer reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            review.removeLike();
            reviewStorage.deleteRating(reviewId, userId);
            reviewStorage.updateRating(review);
        } else {
            throw new IllegalArgumentException("пользователь " + userId + " не добавлял оценку к отзыву " + reviewId);
        }
    }

    public void deleteDislike(Integer reviewId, Long userId) {
        Review review = getReviewById(reviewId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            review.removeDislike();
            reviewStorage.deleteRating(reviewId, userId);
            reviewStorage.updateRating(review);
        } else {
            throw new IllegalArgumentException("пользователь " + userId + " не добавлял оценку к отзыву " + reviewId);
        }
    }


    private Review getReviewById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new NotFoundException("отзыв с id " + id + " не найден"));
    }

    private void validateUser(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден.", userId));
        }
    }

    private void validateFilm(Long filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.error("Фильм c id:{} не найден", filmId);
            throw new NotFoundException("Фильм c id: " + filmId + " не найден");
        }
    }

}
