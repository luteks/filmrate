package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
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
    public User create(@RequestBody User user) {
        log.debug("Starting user create");
        userCreateValidation(user);

        if ((user.getName() == null) || (user.getName().isBlank())) {
            log.warn("User name doesn't exist. Replacing with user login.");
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("User {} created.", user);
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

    private void userCreateValidation(User user) {
        if (user.getEmail().isBlank()) {
            log.error("User email doesn't exist!");
            throw new ConditionsNotMetException("Почта не может отсутствовать!");
        }
        if (!user.getEmail().contains("@")) {
            log.error("User email doesn't contains '@'!");
            throw new ConditionsNotMetException("Почта должна содержать символ '@'!");
        }
        if (user.getLogin().isBlank()) {
            log.error("User login doesn't exist!");
            throw new ConditionsNotMetException("Логин не может отсутствовать!");
        }
        if (user.getLogin().contains(" ")) {
            log.error("User login contains ' '!");
            throw new ConditionsNotMetException("Логин не может содержать символ ' '!");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("User birthdate in future!");
            throw new ConditionsNotMetException("Дата рождения не может быть в будущем!");
        }

        log.debug("User creation is valid.");
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.debug("Starting user update.");
        userUpdateValidation(newUser);
        User oldUser = users.get(newUser.getId());

        if (!(newUser.getEmail().isBlank()) && (newUser.getEmail().contains("@"))){
            boolean isExist = false;

            for (User user : users.values()) {
                if (user.getEmail().equals(newUser.getEmail())){
                    isExist = true;
                    break;
                }
            }

            if (!isExist) {
                log.debug("User email changed from {} to {}", oldUser.getEmail(), newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
        }
        if (!(newUser.getLogin().isBlank()) && !(newUser.getLogin().contains(" "))) {
            boolean isExist = false;

            for (User user : users.values()) {
                if (user.getLogin().equals(newUser.getLogin())){
                    isExist = true;
                    break;
                }
            }

            if ((!isExist)) {
                log.debug("User login changed from {} to {}", oldUser.getLogin(), newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
        }
        if ((newUser.getName() != null)) {
            if (!(newUser.getName().isBlank())) {
                log.debug("User name changed from {} to {}", oldUser.getName(), newUser.getName());
                oldUser.setName(newUser.getName());
            }
        }
        if (newUser.getBirthday() != null) {
            if (newUser.getBirthday().isBefore(LocalDate.now())) {
                log.debug("User birthdate changed from {} to {}", oldUser.getBirthday(), newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }
        }

        log.info("User with id {} data updated.", oldUser.getId());
        return oldUser;
    }

    private void userUpdateValidation(User user) {
        if (user.getId() == null) {
            log.error("User id not founded!");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!(users.containsKey(user.getId()))) {
            log.error("User id doesn't exist in collection!");
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        log.debug("User update is valid");
    }
}
