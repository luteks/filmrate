package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public Film create(Film film) {
        film = film.toBuilder()
                .id(getNextId())
                .build();

        if (film.getMpa() != null && !mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг фильма не найден " + film.getMpa().getId());
        }
        if (!genreStorage.areGenresExist(
                film.getGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toList()))
        ) {
            throw new NotFoundException("Жанры не найдены " + film.getGenres());
        }

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());

        return film;
    }

    public Film update(Film film) {
        findByIdFromStorage(film.getId());
        if (film.getMpa() != null && !mpaStorage.isMpaExists(film.getMpa().getId())) {
            throw new NotFoundException("Рейтинг фильма не найден " + film.getMpa().getId());
        }
        if (!genreStorage.areGenresExist(
                film.getGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toList()))
        ) {
            throw new NotFoundException("Жанры не найдены " + film.getGenres());
        }

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
        findByIdFromStorage(filmId);
        if (!userStorage.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + "не найден");
        }
        filmStorage.addLikeByUser(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        findByIdFromStorage(filmId);
        if (!userStorage.isUserExists(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        log.info("Начато возвращение топа фильмов.");

        return filmStorage.getTopFilms(count).stream().toList();
    }

    private long getNextId() {
        return filmStorage.getFilms().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

    private Film findByIdFromStorage(Long filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            throw new NotFoundException("Фильм c id: " + filmId + " не найден");
        }
        return filmStorage.findById(filmId);
    }
}