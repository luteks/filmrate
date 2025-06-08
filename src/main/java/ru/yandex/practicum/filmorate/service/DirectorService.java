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
        log.info("запрос всех пользователей");
        return directorStorage.findAll();
    }

    public Director findById(int id) {
        log.info("поиск по id{}", id);
        return getDirectorOrThrow(id);
    }

    public Director create(Director director) {
        log.info("добавление режиссера {}", director);
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        getDirectorOrThrow(director.getId());
        log.info("обновление режиссера{}", director);
        return directorStorage.update(director);
    }

    public void deleteById(int id) {
        log.info("удаление режиссера с id{}", id);
        directorStorage.deleteById(id);
    }

    public void deleteAll() {

        directorStorage.deleteAll();
        log.info("Таблица director была очищена");
    }

    private Director getDirectorOrThrow(Integer directorId) {
        if (!directorStorage.isDirectorExist(directorId)) {
            log.error("Режиссер c id:{} не найден", directorId);
            throw new NotFoundException("Режиссер c id: " + directorId + " не найден");
        }

        return directorStorage.findById(directorId);
    }
}
