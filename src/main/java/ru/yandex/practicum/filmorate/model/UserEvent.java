package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class UserEvent {
    private Long eventId;
    @NotNull
    private Long userId;
    @Enumerated(EnumType.STRING)
    private EventType eventType;  // LIKE, REVIEW, FRIEND
    @Enumerated(EnumType.STRING)
    private Operation operation;  // ADD, REMOVE, UPDATE
    private Long entityId;  // ID фильма, отзыва, друга и т. д.
    private Long timestamp;
}
