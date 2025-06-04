package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcOperations jdbc;

    private static UserEvent mapRowToUserEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return UserEvent.builder()
                .eventId(resultSet.getLong("event_id"))
                .userId(resultSet.getLong("user_id"))
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getLong("entity_id"))
                .timestamp(resultSet.getTimestamp("timestamp").getTime())
                .build();
    }

    @Override
    public Collection<UserEvent> getFeed(Long id) {
        String sql = """
            SELECT event_id, user_id, event_type, operation, entity_id, timestamp
            FROM user_event
            WHERE user_id = :user_id
            ORDER BY event_id
            """;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", id);

        return jdbc.query(sql, parameterSource, FeedDbStorage::mapRowToUserEvent);
    }

    @Override
    public void addEventToFeed(Long userId, EventType eventType, Operation operation, Long entityId) {
        String sql = """
            INSERT INTO user_event (user_id, event_type, operation, entity_id)
            VALUES (:user_id, :event_type, :operation, :entity_id)
            """;

        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("user_id", userId);
        parameterSource.addValue("event_type", eventType.name());
        parameterSource.addValue("operation", operation.name());
        parameterSource.addValue("entity_id", entityId);

        jdbc.update(sql, parameterSource);
    }
}