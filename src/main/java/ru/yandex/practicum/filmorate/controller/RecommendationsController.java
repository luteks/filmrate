package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.UserRecommendationsService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class RecommendationsController {
    private final UserRecommendationsService recommendationsService;

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable long id) {
        return recommendationsService.getRecommendations(id);
    }
}
