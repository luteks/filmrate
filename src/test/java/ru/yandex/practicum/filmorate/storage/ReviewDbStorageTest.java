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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewDbStorage.class, FilmDbStorage.class, UserDbStorage.class})
public class ReviewDbStorageTest {
    private final ReviewDbStorage reviewStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Review review;

    @BeforeEach
    public void setUp() {

        userStorage.create(User.builder()
                .name("myName")
                .email("myEmail@yandex.ru")
                .login("1234567")
                .birthday(LocalDate.of(2000, 10, 10))
                .build());

        filmStorage.create(Film.builder()
                .name("Test name")
                .description("Test description")
                .releaseDate(LocalDate.parse("1980-05-21"))
                .duration(100)
                .mpa(new Mpa(1, "G"))
                .build());

        review = Review.builder()
                .filmId(1L)
                .userId(1L)
                .content("something")
                .build();
    }

    @Test
    public void update_shouldUpdateReview() {
        reviewStorage.create(review);

        Review updatedReview = review;
        updatedReview.setContent("newContent");

        reviewStorage.update(updatedReview);
        Optional<Review> newReview = reviewStorage.findById(1);

        Assertions.assertTrue(newReview.isPresent());
        Assertions.assertEquals(newReview.get().getContent(), updatedReview.getContent());
    }

    @Test
    public void findAll_shouldReturnAllReviews() {
        reviewStorage.create(review);
        Review review2 = review;
        review2.setContent("newContent");
        reviewStorage.create(review2);

        Collection<Review> all = reviewStorage.findAll(2);
        Assertions.assertEquals(all.size(), 2);
    }

    @Test
    void findReviewsByFilmId_shouldReturnAllReviewsOfFilm() {
        reviewStorage.create(review);
        Review review2 = review;
        review2.setContent("newContent");
        reviewStorage.create(review2);

        Collection<Review> all = reviewStorage.findReviewsByFilmId(1L, 2);
        Assertions.assertEquals(all.size(), 2);
    }

    @Test
    void delete_shouldDeleteTheReview() {
        reviewStorage.create(review);
        Review review2 = review;
        review2.setContent("newContent");
        reviewStorage.create(review2);

        reviewStorage.delete(1);
        Assertions.assertEquals(reviewStorage.findAll(10).size(), 1);
    }

    @Test
    void addLike_shouldAddLikeToReview() {
        reviewStorage.create(review);
        reviewStorage.addLike(1, 1L);
        Assertions.assertEquals(reviewStorage.hasUserRatedTheReview(1, 1L), Boolean.TRUE);
        Assertions.assertEquals(reviewStorage.getUserRating(1, 1L), Boolean.TRUE);
    }

    @Test
    void addDislike_shouldAddDislikeToReview() {
        reviewStorage.create(review);
        reviewStorage.addDislike(1, 1L);
        Assertions.assertEquals(reviewStorage.hasUserRatedTheReview(1, 1L), Boolean.TRUE);
        Assertions.assertEquals(reviewStorage.getUserRating(1, 1L), Boolean.FALSE);
    }

    @Test
    void deleteRating_shouldDeleteUsersRate() {
        reviewStorage.create(review);
        reviewStorage.addDislike(1, 1L);
        Assertions.assertEquals(reviewStorage.hasUserRatedTheReview(1, 1L), Boolean.TRUE);
        reviewStorage.deleteRating(1, 1L);
        Assertions.assertEquals(reviewStorage.hasUserRatedTheReview(1, 1L), Boolean.FALSE);
    }


}
