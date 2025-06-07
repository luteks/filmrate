package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.annotation.Nonnegative;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmRatingsController {
    private final FilmService filmService;

    @Autowired
    public FilmRatingsController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Long filmId,
                        @PathVariable Long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Long filmId,
                           @PathVariable Long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") @Nonnegative int count,
                               @RequestParam(required = false) Long genreId,
                               @RequestParam(required = false) Integer year) {
        return filmService.getTopFilms(count, genreId, year);
    }
}
