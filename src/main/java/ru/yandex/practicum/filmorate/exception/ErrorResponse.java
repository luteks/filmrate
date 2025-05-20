package ru.yandex.practicum.filmorate.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String error;
    private final String description;
    private final Map<String, String> fields;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
        this.fields = null;
    }

    public ErrorResponse(String error, Map<String, String> fields) {
        this.error = error;
        this.fields = fields;
        this.description = null;
    }
}
