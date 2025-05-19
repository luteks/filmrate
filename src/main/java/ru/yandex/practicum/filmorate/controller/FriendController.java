package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users/{id}/friends")
public class FriendController {
    private final UserService userService;

    @Autowired
    public FriendController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{friendId}")
    public void addFriend(@PathVariable("id") Long userId,
                          @PathVariable Long friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable("id") Long userId,
                             @PathVariable Long friendId) {
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping
    public Collection<User> getFriends(@PathVariable("id") Long userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/common/{otherId}")
    public List<User> commonFriends(@PathVariable("id") Long userId,
                                    @PathVariable("otherId") Long friendId) {
        return userService.commonFriends(userId, friendId);
    }
}
