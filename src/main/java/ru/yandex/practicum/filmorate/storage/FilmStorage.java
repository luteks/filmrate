package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Collection<Film> getFilms();

    Film findById(Long filmId);

    void delete(Long filmId);

    void deleteAll();

    void addLikeByUser(Long filmId, Long userId);

    Collection<Film> getTopFilms(Integer count, Long genreId, Integer year);

    Collection<Film> getCommonFilms(Long userId, Long friendId);

    void removeLike(Long filmId, Long userId);

    boolean isFilmExists(Long filmId);

    List<Film> searchByTitle(String substring);

    List<Film> searchByDirector(String substring);

    List<Film> searchByBoth(String substring);

    Collection<Film> getFilmsByDirectorId(int directorId);

    List<Film> findRecommendations(Long similarUserId, Long userId);
}