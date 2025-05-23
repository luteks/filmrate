package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> getAllGenre() {
        return genreStorage.findAll();
    }

    public Genre getGenre(int genreId) {
        if (!genreStorage.isGenreExists(genreId)) {
            throw new NotFoundException("Жанр с id " + genreId + "не найден");
        }
        return genreStorage.getGenreById(genreId);
    }
}