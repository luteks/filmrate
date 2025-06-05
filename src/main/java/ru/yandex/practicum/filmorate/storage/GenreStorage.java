package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreStorage {
    Genre getGenreById(Long genreId);

    Collection<Genre> findAll();

    boolean isGenreExists(Long genreId);

    boolean areGenresExist(List<Long> genreIds);

    void delete(Long id);

    void deleteAll();
}