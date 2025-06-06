package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
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
    private final FeedStorage feedStorage;

    public Review findById(Long id) {
        log.info("Начат поиск отзыва с id = {}", id);

        if (!reviewStorage.isReviewExist(id)) {
            log.error("Отзыв с id={} не найден", id);
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }

        return reviewStorage.findById(id);
    }

    public Collection<Review> findAll(int limit) {
        log.info("Вывод списка отзывов размером {}", limit);
        return reviewStorage.findAll(limit);
    }

    public Collection<Review> findReviewsOfFilm(Long filmId, int limit) {
        log.info("Начат поиск отзывов по filmId = {}", filmId);
        return reviewStorage.findReviewsByFilmId(filmId, limit);
    }

    public Review create(Review review) {
        validateReview(review);
        Review reviewCreated = reviewStorage.create(review);
        log.info("создан новый отзыв {}", review);

        feedStorage.addEventToFeed(reviewCreated.getUserId(), EventType.REVIEW, Operation.ADD, reviewCreated.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} добавил отзыв с id: {}",
                reviewCreated.getUserId(), reviewCreated.getReviewId());

        return reviewCreated;
    }

    public Review update(Review review) {
        validateReview(review);
        Review updatedReview = reviewStorage.update(review);
        log.info("отзыв обновлен {}", review);

        feedStorage.addEventToFeed(updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, updatedReview.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} обновил отзыв с id: {}",
                updatedReview.getUserId(), updatedReview.getReviewId());

        return updatedReview;
    }

    public void delete(Long id) {
        Review deletedReview = findById(id);
        reviewStorage.delete(id);

        feedStorage.addEventToFeed(deletedReview.getUserId(), EventType.REVIEW, Operation.REMOVE, deletedReview.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} удалил отзыв с id: {}",
                deletedReview.getUserId(), id);
    }

    public void addLike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        validateUser(userId);
        Review review = reviewStorage.findById(reviewId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            log.warn("Лайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
            throw new IllegalArgumentException("пользователь " + userId + " уже ставил лайк отзыву " + reviewId);
        } else if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            review.removeDislike();
            reviewStorage.deleteRating(reviewId, userId);
            log.info("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
        }
        reviewStorage.addLike(reviewId, userId);
        review.addLike();
        log.info("Добавлен лайк для отзыва {} от пользователя {}", reviewId, userId);

        feedStorage.addEventToFeed(userId, EventType.LIKE, Operation.ADD, reviewId);
        log.info("Событие добавлено в ленту: пользователь с id: {} лайкнул отзыв с id: {}",
                userId, reviewId);

        reviewStorage.updateRating(review);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        validateUser(userId);
        Review review = reviewStorage.findById(reviewId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            log.warn("Дизлайк уже существует для отзыва {} от пользователя {}", reviewId, userId);
            throw new IllegalArgumentException("пользователь " + userId + " уже ставил дизлайк отзыву " + reviewId);
        } else if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            review.removeLike();
            reviewStorage.deleteRating(reviewId, userId);
            log.info("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);
        }
        reviewStorage.addDislike(reviewId, userId);
        review.addDislike();

        log.info("Добавлен дизлайк для отзыва {} от пользователя {}", reviewId, userId);

        feedStorage.addEventToFeed(userId, EventType.LIKE, Operation.REMOVE, reviewId);
        log.info("Событие добавлено в ленту: пользователь с id: {} удалил лайк с отзыва с id: {}",
                userId, reviewId);

        reviewStorage.updateRating(review);
    }

    public void deleteLike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && reviewStorage.getUserRating(reviewId, userId)) {
            Review review = reviewStorage.findById(reviewId);
            review.removeLike();
            reviewStorage.deleteRating(reviewId, userId);
            reviewStorage.updateRating(review);
            log.info("Удалён лайк для отзыва {} от пользователя {}", reviewId, userId);
        } else {
            log.warn("Лайк не найден для отзыва {} от пользователя {}", reviewId, userId);
            throw new IllegalArgumentException("пользователь " + userId + " не добавлял оценку к отзыву " + reviewId);
        }
    }

    public void deleteDislike(Long reviewId, Long userId) {
        validateLikeInput(reviewId, userId);
        validateUser(userId);
        if (reviewStorage.hasUserRatedTheReview(reviewId, userId) && !reviewStorage.getUserRating(reviewId, userId)) {
            Review review = reviewStorage.findById(reviewId);
            review.removeDislike();
            reviewStorage.deleteRating(reviewId, userId);
            reviewStorage.updateRating(review);
            log.info("Удалён дизлайк для отзыва {} от пользователя {}", reviewId, userId);
        } else {
            log.warn("Дизлайк не найден для отзыва {} от пользователя {}", reviewId, userId);
            throw new IllegalArgumentException("пользователь " + userId + " не добавлял оценку к отзыву " + reviewId);
        }
    }

    private void validateReview(Review review) {
        if (review.getUserId() == null) {
            throw new ValidationException("Пользователь не указан (userId=null)");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Фильм не указан (filmId=null)");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Тип отзыва не указан (isPositive=null)");
        }
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Содержание отзыва не указано (content=null)");
        }

        validateUser(review.getUserId());

        validateFilm(review.getFilmId());
    }

    private void validateLikeInput(Long reviewId, Long userId) {
        if (!reviewStorage.isReviewExist(reviewId)) {
            log.error("Отзыв с id={} не найден.", reviewId);
            throw new ValidationException("Отзыв с id=" + reviewId + " не найден");
        }
        validateUser(userId);
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
