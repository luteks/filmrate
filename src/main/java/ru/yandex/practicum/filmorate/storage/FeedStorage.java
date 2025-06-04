package ru.yandex.practicum.filmorate.storage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.Collection;

public interface FeedStorage {
    Collection<UserEvent> getFeed(Long id);

    void addEventToFeed(Long userId, EventType eventType, Operation operation, Long entityId);
}