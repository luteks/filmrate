package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
public class User {
    private Long id;
    private String name;
    private LocalDate birthday;

    @NonNull
    private String email;

    @NonNull
    private String login;
}
