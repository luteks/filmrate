package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final Integer DESCRIPTION_LENGTH = 200;
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Film collection {} successful returned." + films.values());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Start film creating.");
        filmCreateValidation(film);
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Film {} created." + film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void filmCreateValidation(Film film) {
        if (film.getName().isBlank()) {
            log.error("Film name is empty!");
            throw new ConditionsNotMetException("Название не может быть пустым!");
        }
        if (film.getDescription().length() > DESCRIPTION_LENGTH) {
            log.error("Film description more than {} chars!", DESCRIPTION_LENGTH);
            throw new ConditionsNotMetException("Максимальная длинна описания - " + DESCRIPTION_LENGTH + " символов! Обнаружено символов: " + film.getDescription().length());
        }
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Release date is before first film date!");
            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года!");
        }
        if (film.getDuration() < 0) {
            log.error("Film duration can't be negative value!");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом.");
        }

        log.debug("Film creation is valid.");
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.debug("Starting film update.");
        filmUpdateValidation(newFilm);
        Film oldFilm = films.get(newFilm.getId());

        if (newFilm.getDescription() != null) {
            if (newFilm.getDescription().length() < DESCRIPTION_LENGTH) {
                log.debug("Film description changed from {} to {}.", oldFilm.getDescription(), newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
        }
        if (!(newFilm.getName().isBlank())) {
            log.debug("Film name changed from {} to {}.", oldFilm.getName(), newFilm.getName());
            oldFilm.setName(newFilm.getName());
        }
        if (newFilm.getReleaseDate() != null) {
            if (newFilm.getReleaseDate().isAfter(CINEMA_BIRTHDAY)) {
                log.debug("Film release date changed from {} to {}", oldFilm.getReleaseDate(), newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
        }
        if (newFilm.getDuration() != null) {
            if (newFilm.getDuration() > 0) {
                log.debug("Film duration changed from {} to {}", oldFilm.getDuration(), newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
            }
        }
        log.info("Film with id {} data updated.", oldFilm.getId());
        return oldFilm;
    }

    private void filmUpdateValidation(Film film) {
        if (film.getId() == null) {
            log.error("Film id not founded!");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!(films.containsKey(film.getId()))) {
            log.error("Film id doesn't exist in collection!");
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        log.debug("Film update is valid.");
    }
}
