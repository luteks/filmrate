package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    public Optional<Director> findById(int id);

    public Collection<Director> findAll();

    public Director create(Director director);

    public Director update(Director director);

    public void deleteById(int id);
}
