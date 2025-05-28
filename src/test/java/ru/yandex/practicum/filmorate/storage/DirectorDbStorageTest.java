package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorDbStorage.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DirectorDbStorageTest {

    private final DirectorDbStorage directorDbStorage;

    private Director director;

    @BeforeEach
    public void setUp() {
        director = Director.builder()
                .name("Sergio Leone")
                .build();
    }

    @Test
    public void findById_forExistingId_shouldReturnOptionalOfDirector() {
        directorDbStorage.create(director);
        Optional<Director> director1 = directorDbStorage.findById(1);

        Assertions.assertTrue(director1.isPresent());
        Assertions.assertEquals(director1.get(), director);
    }

    @Test
    public void findById_forNonExistingId_shouldReturnOptionalEmpty() {
        directorDbStorage.create(director);
        Optional<Director> director1 = directorDbStorage.findById(999);

        Assertions.assertTrue(director1.isEmpty());
    }

    @Test
    public void findAll_shouldReturnAllDirectors() {
        directorDbStorage.create(director);
        Director director2 = director;
        director2.setName("Christopher Nolan");
        directorDbStorage.create(director2);

        Collection<Director> directors = directorDbStorage.findAll();

        Assertions.assertEquals(directors.size(), 2);
    }

    @Test
    public void delete_shouldDeleteDirectorById() {
        directorDbStorage.create(director);
        Optional<Director> directorOptional = directorDbStorage.findById(1);

        Assertions.assertTrue(directorOptional.isPresent());

        directorDbStorage.deleteById(1);
        Optional<Director> directorOptional2 = directorDbStorage.findById(1);
        Assertions.assertTrue(directorOptional2.isEmpty());
    }

    @Test
    public void update_shouldUpdateDirector() {
        directorDbStorage.create(director);

        Director director2 = director;
        director2.setName("Christopher Nolan");

        directorDbStorage.update(director2);

        Director updatedDirector = directorDbStorage.findById(1).get();
        Assertions.assertEquals("Christopher Nolan", updatedDirector.getName());
    }
}
