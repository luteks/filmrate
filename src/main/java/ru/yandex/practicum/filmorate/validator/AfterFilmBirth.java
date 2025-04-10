package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AfterFilmBirthValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterFilmBirth {
    String message() default "Дата релиза не должна быть раньше 28 декабря 1895 года";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
