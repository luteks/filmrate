package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAll() {
        log.info("Возвращен список всех рейтингов фильмов.");

        return mpaStorage.findAll();
    }

    public Mpa getMpa(Long mpaId) {
        if (!mpaStorage.isMpaExists(mpaId)) {
            log.error("Рейтинг с id={} не найден", mpaId);
            throw new NotFoundException("Рейтинг с id=" + mpaId + " не найден");
        }

        log.info("Возвращен рейтинг {}", mpaId);
        return mpaStorage.getMpaById(mpaId);
    }

}