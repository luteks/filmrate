package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
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
        validateMpa(film);
        validateGenres(film);

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());

        return film;
    }

    public Film update(Film film) {
        validateFilm(film.getId());
        validateMpa(film);
        validateGenres(film);

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());

        return film;
    }

    public Film findById(Long filmId) {
        log.debug("Начат поиск фильма по id={}.", filmId);

        return validateFilm(filmId);
    }

    public Collection<Film> getFilms() {
        log.info("Вывод списка всех фильмов.");

        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        validateFilm(filmId);
        validateUser(userId);
        filmStorage.addLikeByUser(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        validateFilm(filmId);
        validateUser(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", userId, filmId);
    }

    public List<Film> getTopFilms(int count) {
        List<Film> filmList = filmStorage.getTopFilms(count).stream().toList();

        log.info("Отправлен список всех фильмов.");
        log.debug("{}",filmList);

        return filmList;
    }

    private Film validateFilm(Long filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.error("Фильм c id:{} не найден", filmId);
            throw new NotFoundException("Фильм c id: " + filmId + " не найден");
        }

        return filmStorage.findById(filmId);
    }

    private void validateUser(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + "не найден");
        }
    }

    private void validateGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        if (!genreStorage.areGenresExist(genreIds)) {
            log.error("Жанры не найдены: {}", genreIds);
            throw new NotFoundException("Жанры не найдены: " + genreIds);
        }
    }

    private void validateMpa(Film film) {
        if (film.getMpa() != null && !mpaStorage.isMpaExists(film.getMpa().getId())) {
            log.error("Рейтинг фильма не найден {}", film.getMpa().getId());
            throw new NotFoundException("Рейтинг фильма не найден " + film.getMpa().getId());
        }
    }
}