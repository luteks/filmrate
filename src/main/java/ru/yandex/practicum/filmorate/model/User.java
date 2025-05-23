package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@EqualsAndHashCode()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    private Long id;

    @NotNull
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotNull
    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^$|^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;

    @NonFinal
    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    @JsonIgnore
    private Set<Long> friends = new HashSet<>();

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("user_id", id);
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday.toString());
        return values;
    }
}