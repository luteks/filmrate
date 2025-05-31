package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecommendationsService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Collection<Film> getRecommendations(Long userId) {
        validateUserExists(userId);

        Collection<Film> recommendations = new ArrayList<>();

        Collection<Film> likedFilms = filmStorage.getLikedFilms(userId);
        if (likedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("понравившиеся фильмы пользователя {}", likedFilms);
        Set<Long> otherLikes = new HashSet<>();
        for (Film likedFilm : likedFilms) {
            otherLikes.addAll(likedFilm.getLikes());
        }
        otherLikes.remove(userId);
        if (otherLikes.isEmpty()){
            return Collections.emptyList();
        }

        Map<Long, Integer> commonLikesMap = new HashMap<>();
        Map<Long, Set<Film>> likedFilmsByUserId = new HashMap<>();
        for (Long id : otherLikes) {
            Set<Film> otherUserLikedFilms = new HashSet<>(filmStorage.getLikedFilms(id));
            likedFilmsByUserId.put(id, otherUserLikedFilms);

            int intersectionsCount = (int) otherUserLikedFilms
                    .stream()
                    .filter(likedFilms::contains)
                    .count();

            commonLikesMap.put(id, intersectionsCount);
        }
        int maxIntersections = Collections.max(commonLikesMap.values());

        for (Long otherId : commonLikesMap.keySet()) {
            if (commonLikesMap.get(otherId) == maxIntersections) {
                likedFilmsByUserId.get(otherId).stream()
                        .filter(otherFilm -> !likedFilms.contains(otherFilm))
                        .forEach(recommendations::add);
            }
        }
        return recommendations;
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден.", userId));
        }
    }

}
