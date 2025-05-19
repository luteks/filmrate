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
        Film oldFilm = filmStorage.getFilms().stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Фильм с id {} не найден", film.getId());
                    return new NotFoundException("Фильм с id " + film.getId() + " не найден");
                });

        film.getMovieRatings().addAll(oldFilm.getMovieRatings());

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());
        return film;
    }

    public Film findById(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userService.findById(userId);

        if (film.getMovieRatings().contains(userId)) {
            throw new IllegalArgumentException("Пользователь уже ставил лайк этому фильму");
        }

        film.getMovieRatings().add(userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userService.findById(userId);

        film.getMovieRatings().remove(userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", user.getName(), film.getName());
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getMovieRatings().size()).reversed())
                .limit(count)
                .toList();
    }

    private long getNextId() {
        return filmStorage.getFilms().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }
}