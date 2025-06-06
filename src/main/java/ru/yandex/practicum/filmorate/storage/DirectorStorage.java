package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Director findById(int id);

    Collection<Director> findAll();

    Director create(Director director);

    Director update(Director director);

    void deleteById(int id);

    void deleteAll();

    boolean isDirectorExist(Integer id);
}
