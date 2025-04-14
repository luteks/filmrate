package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AfterFilmBirthValidator implements ConstraintValidator<AfterFilmBirth, LocalDate> {
    private static final LocalDate DAY_BEFORE_CINEMA_BIRTH = LocalDate.of(1895, 12, 27);

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext context) {
        return !(releaseDate == null) && releaseDate.isAfter(DAY_BEFORE_CINEMA_BIRTH);
    }
}
