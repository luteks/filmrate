package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmController filmController;

    private Film film;

    @BeforeEach
    void setUp() {
        filmController.getFilms().clear();
        film = Film.builder()
                .name("Interstellar")
                .description("Great movie")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
    }

    @Test
    void shouldCreateValidFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()));
    }

    @Test
    void shouldCreateValidFilmWithId() throws Exception {
        Film filmWithId = film.toBuilder().id(1L).build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(filmWithId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.name").value(filmWithId.getName()))
                .andExpect(jsonPath("$.description").value(filmWithId.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(filmWithId.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(filmWithId.getDuration()));
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() throws Exception {
        Film invalidFilm = film.toBuilder().name("").build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("Название не должно быть пустым"));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() throws Exception {
        Film invalidFilm = film.toBuilder().description("A".repeat(201)).build();
        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.description").value("Максимальная длина описания — 200 символов"));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() throws Exception {
        Film invalidFilm = film.toBuilder().releaseDate(LocalDate.of(1800, 1, 1)).build();
        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.releaseDate").value("Дата релиза не должна быть раньше 28 декабря 1895 года"));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() throws Exception {
        Film invalidFilm = film.toBuilder().duration(0).build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.duration").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() throws Exception {
        Film invalidFilm = film.toBuilder().duration(-1).build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.duration").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldThrowExceptionWhenIdNotFound() throws Exception {
        mockMvc.perform(put("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Фильм с id = " + film.getId() + " не найден"));

    }

    @Test
    void shouldThrowExceptionWhenAllFieldsInvalid() throws Exception {
        Film invalidFilm = Film.builder()
                .name("")
                .description("A".repeat(201))
                .releaseDate(LocalDate.of(1800, 1, 1))
                .duration(-1)
                .build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").value("Название не должно быть пустым"))
                .andExpect(jsonPath("$.fields.description").value("Максимальная длина описания — 200 символов"))
                .andExpect(jsonPath("$.fields.releaseDate").value("Дата релиза не должна быть раньше 28 декабря 1895 года"))
                .andExpect(jsonPath("$.fields.duration").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldUpdateValidFilm() throws Exception {
        String createdFilmContent = mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Film createdFilm = objectMapper.readValue(createdFilmContent, Film.class);

        Film filmUpdate = film.toBuilder()
                .id(createdFilm.getId())
                .name("New Name")
                .build();

        mockMvc.perform(put("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(filmUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmUpdate.getId()))
                .andExpect(jsonPath("$.name").value(filmUpdate.getName()))
                .andExpect(jsonPath("$.description").value(filmUpdate.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(filmUpdate.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(filmUpdate.getDuration()));
    }

    @Test
    void shouldReturnCorrectFilm() throws Exception {
        String createdFilmContent = mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Film createdFilm = objectMapper.readValue(createdFilmContent, Film.class);

        mockMvc.perform(get("/films/" + createdFilm.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdFilm.getId()))
                .andExpect(jsonPath("$.name").value(createdFilm.getName()))
                .andExpect(jsonPath("$.description").value(createdFilm.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(createdFilm.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(createdFilm.getDuration()));
    }

    @Test
    void shouldReturnCorrectCollection() throws Exception {
        Film film2 = film.toBuilder().name("Pearl Harbor").build();

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/films")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(film2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(film.getName()))
                .andExpect(jsonPath("$[1].name").value(film2.getName()));
    }
}