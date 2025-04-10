package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Film collection {} successful returned.", films.values());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Starting film create.");
        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Film {} created.", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.debug("Starting film update.");

        if (!(films.containsKey(film.getId()))) {
            log.error("Film id not found in collection!");
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Film with id {} data updated.", film.getId());
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
}
