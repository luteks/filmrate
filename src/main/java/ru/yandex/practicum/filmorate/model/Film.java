package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Film {
    private Long id;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    @NonNull
    private String name;
}
