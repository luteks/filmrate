package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private Long reviewId;
    @NotNull
    @Size(max = 1000)
    private String content;
    @NotNull
    private Boolean isPositive;

    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;

    private int useful;

    public void addLike() {
        useful++;
    }

    public void addDislike() {
        useful--;
    }

    public void removeLike() {
        useful--;
    }

    public void removeDislike() {
        useful++;
    }

}
