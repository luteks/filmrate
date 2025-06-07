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
import java.util.Comparator;
import java.util.List;

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

    public Collection<Review> findReviewsOfFilm(Long filmId, Integer limit) {
        if (filmId != null) {
            checkFilmExist(filmId);
        }

        List<Review> all = (filmId == null)
                ? reviewStorage.getAll()
                : reviewStorage.getByFilmId(filmId);

        return all.stream()
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(limit)
                .toList();
    }

    public Review create(Review review) {
        validateReview(review);
        Review reviewCreated = reviewStorage.create(review);
        log.info("создан новый отзыв {}", review);

        addEventToFeed(reviewCreated, Operation.ADD);

        return reviewCreated;
    }

    public Review update(Review review) {
        validateReview(review);
        Review updatedReview = reviewStorage.update(review);
        log.info("отзыв обновлен {}", review);

        addEventToFeed(updatedReview, Operation.UPDATE);

        return updatedReview;
    }

    public void delete(Long id) {
        Review deletedReview = findById(id);
        reviewStorage.delete(id);

        addEventToFeed(deletedReview, Operation.REMOVE);
    }

    private void addEventToFeed(Review review, Operation operation) {
        feedStorage.addEventToFeed(review.getUserId(), EventType.REVIEW, operation, review.getReviewId());
        log.info("Событие добавлено в ленту: пользователь с id: {} {} с id: {}",
                review.getUserId(), operation,review.getReviewId());
    }

    public void addLike(Long reviewId, Long userId) {
        checkUserExist(userId);
        checkReviewExist(reviewId);

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

        reviewStorage.updateRating(review);
    }

    public void addDislike(Long reviewId, Long userId) {
        checkUserExist(userId);
        checkReviewExist(reviewId);

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

        reviewStorage.updateRating(review);
    }

    public void deleteLike(Long reviewId, Long userId) {
        checkUserExist(userId);
        checkReviewExist(reviewId);

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
        checkUserExist(userId);
        checkReviewExist(reviewId);

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

        checkUserExist(review.getUserId());

        checkFilmExist(review.getFilmId());
    }

    private void checkReviewExist(Long reviewId) {
        if (!reviewStorage.isReviewExist(reviewId)) {
            log.error("Отзыв с id={} не найден.", reviewId);
            throw new ValidationException("Отзыв с id=" + reviewId + " не найден");
        }
    }

    private void checkUserExist(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден.", userId));
        }
    }

    private void checkFilmExist(Long filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.error("Фильм c id:{} не найден", filmId);
            throw new NotFoundException("Фильм c id: " + filmId + " не найден");
        }
    }

}
