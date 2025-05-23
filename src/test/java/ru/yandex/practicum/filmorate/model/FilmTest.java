package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();
    private Film film;

    @BeforeEach
    void setUp() {
        film = Film.builder()
                .name("Interstellar")
                .description("Great movie")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
    }

    @Test
    public void testValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }


    @Test
    public void testInvalidFilmDescription() {
        film.setDescription("a".repeat(200));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());

        film.setDescription("a".repeat(199));
        violations = validator.validate(film);
        assertTrue(violations.isEmpty());

        film.setDescription("a".repeat(201));
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Максимальная длина описания — 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidFilmReleaseDate() {
        film.setReleaseDate(LocalDate.of(1985,12,30));
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());

        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        violations = validator.validate(film);
        assertTrue(violations.isEmpty());

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Дата релиза не должна быть раньше 28 декабря 1895 года", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidFilmDuration() {
        film.setDuration(-100);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());

        film.setDuration(0);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());

        film.setDuration(1);
        violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}
