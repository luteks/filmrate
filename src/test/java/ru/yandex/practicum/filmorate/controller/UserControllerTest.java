package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        userController.getUsers().clear();
        user = User.builder()
                .email("test@mail.com")
                .login("username")
                .name("User Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void shouldCreateValidUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void shouldCreateValidUserWithId() throws Exception {
        User userWithId = user.toBuilder().id(1L).build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userWithId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.login").value(userWithId.getLogin()))
                .andExpect(jsonPath("$.name").value(userWithId.getName()))
                .andExpect(jsonPath("$.email").value(userWithId.getEmail()))
                .andExpect(jsonPath("$.birthday").value(userWithId.getBirthday().toString()));

    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() throws Exception {
        User userWithoutName = user.toBuilder().name(null).build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userWithoutName)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userWithoutName.getLogin()));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() throws Exception {
        User invalidUser = user.toBuilder().email("").build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").value("Email не должен быть пустым"));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsInvalid() throws Exception {
        User invalidUser = user.toBuilder().email("invalid-email@").build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").value("Некорректный формат email"));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() throws Exception {
        User invalidUser = user.toBuilder().login("user name").build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.login").value("Логин не должен содержать пробелы"));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() throws Exception {
        User invalidUser = user.toBuilder().birthday(LocalDate.now().plusDays(1)).build();
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.birthday").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldThrowExceptionWhenIdNotFound() throws Exception {
        mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.description").value("Юзер с id " + user.getId() + " не найден"));
    }

    @Test
    void shouldThrowExceptionWhenAllFieldsInvalid() throws Exception {
        User invalidUser = User.builder()
                .email("")
                .login("")
                .name(null)
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").value("Email не должен быть пустым"))
                .andExpect(jsonPath("$.fields.login").value("Логин не должен быть пустым"))
                .andExpect(jsonPath("$.fields.birthday").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldUpdateValidUser() throws Exception {
        String createdUserContent = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        User createdUser = objectMapper.readValue(createdUserContent, User.class);

        User userUpdate = user.toBuilder()
                .id(createdUser.getId())
                .login("new_login")
                .build();

        mockMvc.perform(put("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userUpdate.getId()))
                .andExpect(jsonPath("$.email").value(userUpdate.getEmail()))
                .andExpect(jsonPath("$.login").value(userUpdate.getLogin()))
                .andExpect(jsonPath("$.name").value(userUpdate.getName()))
                .andExpect(jsonPath("$.birthday").value(userUpdate.getBirthday().toString()));
    }

    @Test
    void shouldReturnCorrectUser() throws Exception {
        String createdUserContent = mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        User createdUser = objectMapper.readValue(createdUserContent, User.class);

        mockMvc.perform(get("/users/" + createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(createdUser.getLogin()))
                .andExpect(jsonPath("$.name").value(createdUser.getName()))
                .andExpect(jsonPath("$.email").value(createdUser.getEmail()))
                .andExpect(jsonPath("$.birthday").value(createdUser.getBirthday().toString()));
    }

    @Test
    void shouldReturnCorrectCollection() throws Exception {
        User user2 = user.toBuilder().login("another_login").build();

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].login").value(user.getLogin()))
                .andExpect(jsonPath("$[1].login").value(user2.getLogin()));
    }
}