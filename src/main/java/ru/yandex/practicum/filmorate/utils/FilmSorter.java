package ru.yandex.practicum.filmorate.utils;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.SortType;
import ru.yandex.practicum.filmorate.exception.InvalidSortTypeException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


@Component
public class FilmSorter {
    private final Map<SortType, Comparator<Film>> comparatorBySortType = new HashMap<>();

    public FilmSorter() {
        comparatorBySortType.put(SortType.YEAR, Comparator.comparing(Film::getReleaseDate));
        comparatorBySortType.put(SortType.LIKES, Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed());
    }

    public Comparator<Film> getComparator(String sortType) {
        if (sortType.isBlank() || !Arrays.stream(SortType.values()).map(Enum::name).anyMatch(name -> name.equalsIgnoreCase(sortType))) {
            throw new InvalidSortTypeException("неверный тип сортировки " + sortType);
        }
        return comparatorBySortType.get(SortType.valueOf(sortType.toUpperCase()));
    }

}
