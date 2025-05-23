package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public Collection<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    public Mpa getMpa(int mpaId) {
        if (!mpaStorage.isMpaExists(mpaId)) {
            throw new NotFoundException("Рейтинг не найден");
        }
        return mpaStorage.getMpaById(mpaId);
    }
}