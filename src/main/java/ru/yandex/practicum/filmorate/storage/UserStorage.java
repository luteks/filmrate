package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Collection<User> getUsers();

    User getUserById(Long userId);

    void delete(Long userId);

    void deleteAll();

    void addFriend(Long userId, Long friendId);

    Collection<User> getFriends(Long userId);

    Collection<User> getCommonFriends(Long firstUserId, Long secondUserId);

    void deleteFriend(Long userId, Long friendId);

    boolean isUserExists(Long userId);

    boolean isUserExistsWithEmail(User user);
}