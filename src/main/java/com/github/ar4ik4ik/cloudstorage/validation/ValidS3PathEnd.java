package com.github.ar4ik4ik.cloudstorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = S3PathEndValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidS3PathEnd {
    String message() default "Path must be empty or end with '/'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
