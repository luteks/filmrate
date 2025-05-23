package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreStorage genreStorage;

    public Collection<Genre> getAllGenre() {
        log.info("Возвращен список всех жанров фильмов.");

        return genreStorage.findAll();
    }

    public Genre getGenre(int genreId) {
        if (!genreStorage.isGenreExists(genreId)) {
            log.error("Жанр с id={} не найден", genreId);
            throw new NotFoundException("Жанр с id " + genreId + "не найден");
        }

        log.info("Возвращен жанр {}", genreId);
        return genreStorage.getGenreById(genreId);
    }
}