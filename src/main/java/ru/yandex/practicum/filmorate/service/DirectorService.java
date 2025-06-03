package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> findAll() {
        log.debug("запрос всех пользователей");
        return directorStorage.findAll();
    }

    public Director findById(int id) {
        log.debug("поиск по id{}", id);
        return getDirectorById(id);
    }

    public Director create(Director director) {
        log.debug("добавление режиссера {}", director);
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        Director oldDirector = getDirectorById(director.getId());
        log.debug("обновление режиссера{}", director);
        return directorStorage.update(director);
    }

    public void deleteById(int id) {
        log.debug("удаление режиссера с id{}", id);
        directorStorage.deleteById(id);
    }


    private Director getDirectorById(int id) {
        return directorStorage.findById(id).orElseThrow(() -> new NotFoundException("режиссер с id " + id + " не найден"));
    }


}
