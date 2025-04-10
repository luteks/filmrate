package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void testValidFilm() {
        Film film = new Film(1L,"Описание прекрасного фильма" , LocalDate.of(2025, 4, 21),21, "Inception");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidFilmName() {
        Film film = new Film(1L, "Описание пустого фильма", LocalDate.of(2022,1,1), 1,"");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());

        film.setName(null);
        violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        assertEquals("Название фильма не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidFilmDescription() {
        Film film = new Film(1L, "a".repeat(200), LocalDate.of(2032, 2, 2), 1,"Фильм");

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
        Film film = new Film(1L, "a", LocalDate.of(1985, 12, 30), 1, "Фильм");

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
        Film film = new Film(1L, "a", LocalDate.of(1985, 12, 30), -100, "Фильм");

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
