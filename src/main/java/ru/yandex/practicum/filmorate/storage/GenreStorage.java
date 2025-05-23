package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreStorage {
    Genre getGenreById(Integer genreId);

    Collection<Genre> findAll();

    boolean isGenreExists(Integer genreId);

    boolean areGenresExist(List<Integer> genreIds);
}