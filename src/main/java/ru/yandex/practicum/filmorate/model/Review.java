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
    private Integer reviewId;
    @NotNull
    @Size(max = 1000)
    private String content;

    private Boolean isPositive;

    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;

    private Integer useful;

    public void addLike() {
        useful++;
        isPositive = useful > 0;
    }

    public void addDislike() {
        useful--;
        isPositive = useful > 0;
    }

    public void removeLike() {
        useful--;
        isPositive = useful > 0;
    }

    public void removeDislike() {
        useful++;
        isPositive = useful > 0;
    }

}
