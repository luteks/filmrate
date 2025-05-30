package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Collection<Film> getFilms();

    Film findById(Long filmId);

    void deleteFilmById(Long filmId);

    void addLikeByUser(Long filmId, Long userId);

    Collection<Film> getTopFilms(Integer count);

    void removeLike(Long filmId, Long userId);

    boolean isFilmExists(Long filmId);

    List<Film> searchByTitle(String substring);

    List<Film> searchByDirector(String substring);

    List<Film> searchByBoth(String substring);
}