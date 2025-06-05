package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, GenreDbStorage.class, FilmDbStorage.class, MpaDbStorage.class})
public class FilmDbStorageTest {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    Film film;

    @BeforeEach
    void setUp() {
        film = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());
    }

    @Test
    void testCreateFilm() {
        assertNotNull(film.getName());
        assertEquals("Test name", film.getName());
        assertEquals(1L, filmStorage.getFilms().size());
    }

    @Test
    void testUpdateFilm() {
        film = filmStorage.update(Film.builder()
                .name("New test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        assertEquals("New test name", film.getName());
        assertEquals(1, filmStorage.getFilms().size());
    }

    @Test
    void testFindAllFilms() {
        Film newFilm = filmStorage.create(Film.builder()
                .name("New test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1985-02-01"))
                .duration(170)
                .mpa(new Mpa(1L, "G"))
                .build());

        assertNotNull(filmStorage.getFilms());
        assertEquals(2, filmStorage.getFilms().size());
    }

    @Test
    void testFindFilmById() {
        film = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        assertNotNull(film);
        assertEquals(7L, film.getId());
    }

    @Test
    void testIsFilmExists() {
        film = filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1L, "G"))
                .build());

        assertTrue(filmStorage.isFilmExists(film.getId()));
    }
}