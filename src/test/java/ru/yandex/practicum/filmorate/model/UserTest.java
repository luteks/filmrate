package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    public void testValidUser() {
        User user = new User(1L, "user@test.com", "test", "tester", LocalDate.of(2000, 1, 1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidUserEmail() {
        User user = new User(1L, "usertest.com", "test", "tester", LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Электронная почта должна содержать символ @", violations.iterator().next().getMessage());

        user.setEmail("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());

        user.setEmail(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidUserLogin() {
        User user = new User(1L, "user@test.com", "test er", "tester", LocalDate.of(2000, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не должен содержать пробелы", violations.iterator().next().getMessage());

        user.setLogin("");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());

        user.setLogin(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testInvalidUserBirthday() {
        User user = new User(1L, "user@test.com", "tester", "tester", LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());

        user.setBirthday(LocalDate.now());
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());

        user.setBirthday(LocalDate.now().minusDays(1));
        violations = validator.validate(user);
        assertTrue(violations.isEmpty());

        user.setBirthday(null);
        violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }
}
