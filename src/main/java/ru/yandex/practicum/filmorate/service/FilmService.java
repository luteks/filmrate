package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.utils.FilmSorter;

import java.time.LocalDate;
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
    private final FeedStorage feedStorage;
    private final DirectorStorage directorStorage;

    public Film create(Film film) {
        checkMpaExist(film);
        checkGenresExist(film);

        filmStorage.create(film);
        log.info("Добавлен новый фильм \"{}\" c id {}", film.getName(), film.getId());

        return film;
    }

    public Film update(Film film) {
        checkFilmExist(film.getId());
        checkMpaExist(film);
        checkGenresExist(film);

        filmStorage.update(film);
        log.info("Фильм c id {} обновлен", film.getId());

        return film;
    }

    public Film findById(Long filmId) {
        log.debug("Начат поиск фильма по id={}.", filmId);

        return checkFilmExist(filmId);
    }

    public Collection<Film> getFilms() {
        log.info("Вывод списка всех фильмов.");

        return filmStorage.getFilms();
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmExist(filmId);
        checkUserExist(userId);
        filmStorage.addLikeByUser(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму \"{}\"", userId, filmId);

        addEventToFeed(userId, filmId, Operation.ADD);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkFilmExist(filmId);
        checkUserExist(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк фильму \"{}\"", userId, filmId);

        addEventToFeed(userId, filmId, Operation.REMOVE);
    }

    private void addEventToFeed(Long userId, Long filmId, Operation operation) {
        feedStorage.addEventToFeed(userId, EventType.LIKE, operation, filmId);
        log.info("Событие добавлено в ленту: пользователь с id: {} {} лайк у фильма с id: {}", userId, operation, filmId);
    }

    public List<Film> getTopFilms(int count, Long genreId, Integer year) {
        if (genreId != null) {
            checkGenreExist(genreId);
        }
        if (year != null && year < 1895 && year > LocalDate.now().getYear()) {
            log.error("Несуществующий год фильма {}", year);
            throw new ValidationException("Несуществующий год фильма " + year);
        }
        List<Film> filmList = filmStorage.getTopFilms(count, genreId, year).stream().toList();

        log.info("Отправлен список всех фильмов.");
        log.debug("{}", filmList);

        return filmList;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        checkUserExist(userId);
        checkUserExist(friendId);

        List<Film> filmList = filmStorage.getCommonFilms(userId, friendId).stream().toList();

        log.info("Отправлен список общих фильмов Пользователя {} и Пользователя {}", userId, friendId);
        return filmList;
    }

    public Collection<Film> getSortedFilmsByDirectorId(int directorId, String sortType) {
        checkDirectorExist(directorId);
        Collection<Film> films = filmStorage.getFilmsByDirectorId(directorId);
        log.debug("запрос фильмов режиссера с id{}", directorId);
        log.debug("тип сортировки {}", sortType);
        return films.stream()
                .filter(film -> film.getDirectors().stream().anyMatch(d -> d.getId() == directorId))
                .sorted(new FilmSorter().getComparator(sortType))
                .toList();
    }

    public void delete(Long id) {
        checkFilmExist(id);
        filmStorage.delete(id);
        log.info("Был удалён фильм с id: {}", id);
    }

    public void deleteAll() {
        filmStorage.deleteAll();
        log.info("Таблица film была очищена");
    }

    private Film checkFilmExist(Long filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.error("Фильм c id:{} не найден", filmId);
            throw new NotFoundException("Фильм c id: " + filmId + " не найден");
        }

        return filmStorage.findById(filmId);
    }

    private void checkUserExist(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + "не найден");
        }
    }

    private void checkGenresExist(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        List<Long> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        if (!genreStorage.areGenresExist(genreIds)) {
            log.error("Жанры не найдены: {}", genreIds);
            throw new NotFoundException("Жанры не найдены: " + genreIds);
        }
    }

    private void checkGenreExist(Long genreId) {
        if (!genreStorage.isGenreExists(genreId)) {
            log.error("Жанры не найдены: {}", genreId);
            throw new NotFoundException("Жанры не найдены: " + genreId);
        }
    }

    private void checkMpaExist(Film film) {
        if (film.getMpa() != null && !mpaStorage.isMpaExists(film.getMpa().getId())) {
            log.error("Рейтинг фильма не найден {}", film.getMpa().getId());
            throw new NotFoundException("Рейтинг фильма не найден " + film.getMpa().getId());
        }
    }

    private void checkDirectorExist(Integer id) {
        if (!directorStorage.isDirectorExist(id)) {
            log.error("Директор фильма не найден {}", id);
            throw new NotFoundException("Директор фильма не найден " + id);
        }
    }

    public List<Film> search(String query, String by) {
        if (query == null || query.isBlank() || by == null || by.isBlank()) {
            return getTopFilms(10, null, null);
        }

        String[] searchCriteria = by.split(",");

        boolean searchByTitle = false;
        boolean searchByDirector = false;

        for (String criteria : searchCriteria) {
            switch (criteria.trim().toLowerCase()) {
                case "title":
                    searchByTitle = true;
                    log.info("Поиск по названию {}", searchByTitle);
                    break;
                case "director":
                    searchByDirector = true;
                    log.info("Поиск по режиссеру {}", searchByDirector);
                    break;
                default:
                    log.error("Неверные параметры строки запроса поиска");
                    throw new NotFoundException("Неверные параметры строки запроса: " + criteria);
            }
        }

        if (searchByTitle && searchByDirector) {
            return filmStorage.searchByBoth(query);
        } else if (searchByTitle) {
            return filmStorage.searchByTitle(query);
        } else if (searchByDirector) {
            return filmStorage.searchByDirector(query);
        } else return getTopFilms(10, null,null);
    }
}