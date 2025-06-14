package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FeedStorage feedStorage;

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.create(user);
        log.info("Добавлен новый юзер \"{}\" c id {}", user.getLogin(), user.getId());
        return createdUser;
    }

    public User update(User user) {
        findByIdFromStorage(user.getId());
        validateEmail(user);

        User updatedUser = userStorage.update(user);
        log.info("Юзер c id {} обновлен", user.getId());
        return updatedUser;
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
        checkYourself(userId, friendId);

        findByIdFromStorage(userId);
        findByIdFromStorage(friendId);

        userStorage.addFriend(userId, friendId);
        log.info("{} и {} теперь друзья!", userId, friendId);

        addEventToFeed(userId, friendId, Operation.ADD);
    }

    public void deleteFriend(Long userId, Long friendId) {
        checkYourself(userId, friendId);

        findByIdFromStorage(userId);
        findByIdFromStorage(friendId);

        userStorage.deleteFriend(userId, friendId);
        log.info("{} и {} больше не друзья!", userId, friendId);

        addEventToFeed(userId, friendId, Operation.REMOVE);
    }

    private void addEventToFeed(Long userId, Long friendId, Operation operation) {
        feedStorage.addEventToFeed(userId, EventType.FRIEND, operation, friendId);
        log.info("Событие добавлено в ленту: пользователь с id: {} {} друга с id: {}", userId, operation, friendId);
    }

    public List<User> commonFriends(Long userId, Long friendId) {
        findByIdFromStorage(userId);
        findByIdFromStorage(friendId);

        log.debug("Выведены общие друзья пользователей {} и {}", userId, friendId);

        return userStorage.getCommonFriends(userId, friendId).stream().toList();
    }

    public List<User> getFriends(Long userId) {
        findByIdFromStorage(userId);

        log.debug("Выведен список друзей пользователя {}", userId);

        return userStorage.getFriends(userId).stream().toList();
    }

    private void checkYourself(Long id, long friendId) {
        if (id.equals(friendId)) {
            log.error("Пользователь {} не может добавить в друзья сам себя ", id);
            throw new ValidationException("Пользователь id=" + id + " не может добавить в друзья сам себя ");
        }
    }

    private User findByIdFromStorage(Long userId) {
        if (!userStorage.isUserExists(userId)) {
            log.error("Пользователь с id={} не найден.", userId);
            throw new NotFoundException(String.format("Пользователь с id=%s не найден.", userId));
        }
        return userStorage.getUserById(userId);
    }

    private void validateEmail(User user) {
        if (userStorage.isUserExistsWithEmail(user)) {
            log.error("Пользователь с таким e-mail={} уже существует ", user.getEmail());
            throw new ValidationException("Пользователь с таким e-mail уже существует " + user.getEmail());
        }
    }

    public List<UserEvent> getFeed(Long userId) {
        findByIdFromStorage(userId);

        log.info("Получена лента событий пользователя с id: {}", userId);
        return feedStorage.getFeed(userId).stream().toList();
    }

    public void delete(Long id) {
        findByIdFromStorage(id);
        userStorage.delete(id);
        log.info("Был удалён пользователь с id: {}", id);
    }

    public void deleteAll() {
        userStorage.deleteAll();
        log.info("Таблица users была очищена");
    }
}