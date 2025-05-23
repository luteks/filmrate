package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        userStorage.create(user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return user;
    }

    public User update(User user) {
        findByIdFromStorage(user.getId());

        if (userStorage.isUserExistsWithEmail(user)) {
            throw new ValidationException("Пользователь с таким e-mail уже существует " + user.getEmail());
        }

        userStorage.update(user);
        log.info("Юзер c id {} обновлен", user.getId());
        return user;
    }

    public User findById(Long userId) {
        log.info("Произведен поиск пользователя в списке.");
        return findByIdFromStorage(userId);
    }

    public Collection<User> getUsers() {
        log.info("Начато выполнение отправки списка пользователей.");
        return userStorage.getUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить в друзья сам себя " + userId);
        }
        User user = findByIdFromStorage(userId);
        User friend = findByIdFromStorage(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("{} и {} теперь друзья!", user.getName(), friend.getName());
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить самого себя из друзей");
        }

        User user = findByIdFromStorage(userId);
        User friend = findByIdFromStorage(friendId);

        userStorage.deleteFriend(userId, friendId);
        log.info("{} и {} больше не друзья!", user.getName(), friend.getName());
    }

    public List<User> commonFriends(Long userId, Long friendId) {
        User user = findByIdFromStorage(userId);
        User friend = findByIdFromStorage(friendId);

        log.debug("Выведены общие друзья пользователей {} и {}", userId, friendId);

        return userStorage.getCommonFriends(userId, friendId).stream().toList();
    }

    public List<User> getFriends(Long userId) {
        User user = findByIdFromStorage(userId);

        log.debug("Выведен список друзей пользователя {}", userId);

        return userStorage.getFriends(userId).stream().toList();
    }

    private long getNextId() {
        return userStorage.getUsers().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }

    private User findByIdFromStorage(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            throw new NotFoundException(String.format("Пользователь с Id %s не найден.", userId));
        }
        return userStorage.getUserById(userId);
    }
}