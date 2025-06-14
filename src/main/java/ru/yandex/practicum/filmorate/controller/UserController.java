package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") Long userId) {
        return userService.findById(userId);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}/feed")
    public Collection<UserEvent> getFeed(@PathVariable Long id) {
        return userService.getFeed(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @DeleteMapping
    public void deleteAll() {
        userService.deleteAll();
    }
}