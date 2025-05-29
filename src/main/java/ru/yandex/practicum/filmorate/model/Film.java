package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validator.AfterFilmBirth;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    Long id;

    @NotNull
    @NotBlank(message = "Название не должно быть пустым")
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @AfterFilmBirth(message = "Дата релиза не должна быть раньше 28 декабря 1895 года")
    LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    int duration;

    private Set<Long> likes;
    private Mpa mpa;
    private final Set<Genre> genres = new HashSet<>();
    private Set<Director> directors;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("film_id", id);
        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate.toString());
        values.put("duration", duration);
        values.put("rating_id", mpa == null ? null : mpa.getId());
        return values;
    }
}