package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.AfterFilmBirth;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @AfterFilmBirth(message = "Дата релиза не должна быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;
}
