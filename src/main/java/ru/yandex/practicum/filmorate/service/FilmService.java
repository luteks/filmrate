package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;

    @Autowired
    public FilmService(UserService userService, FilmStorage filmStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
        log.trace("Зависимости созданы.");
    }

    public Film create(Film film) {
        film = film.toBuilder()
                .id(getNextId())
                .build();

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());
        return film;
    }

    public Film update(Film film) {
        Film oldFilm = findByIdFromStorage(film.getId());

        film.getMovieRatings().addAll(oldFilm.getMovieRatings());

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    public Film findById(Long filmId) {
        log.info("Начат поиск фильма по id.");
        return findByIdFromStorage(filmId);

    }

    public Collection<Film> getFilms() {
        log.info("Возвращение фильма.");
        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findByIdFromStorage(filmId);
        User user = userService.findById(userId);

        if (film.getMovieRatings().contains(userId)) {
            throw new IllegalArgumentException("Пользователь уже ставил лайк этому фильму");
        }

        film.getMovieRatings().add(userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = findByIdFromStorage(filmId);
        User user = userService.findById(userId);

        film.getMovieRatings().remove(userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public List<Film> getTopFilms(int count) {
        log.info("Начато возвращение топа фильмов.");
        return filmStorage.getTopFilms(count);
    }

    private long getNextId() {
        return filmStorage.getFilms().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

    private Film findByIdFromStorage(long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", filmId);
                    return new NotFoundException("Фильм с id = " + filmId + " не найден");
                });
    }
}