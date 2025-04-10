package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.debug("User collection {} successful returned.", users.values());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("Starting user create");
        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("User {} created.", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.debug("Starting user update.");

        if (!users.containsKey(user.getId())) {
            log.error("User id not found in collection!");
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("User with id {} data updated.", user.getId());
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
