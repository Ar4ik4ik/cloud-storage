package com.github.ar4ik4ik.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;


public class PathValidator implements ConstraintValidator<ValidCharacters, String> {
    private final Pattern NOT_VALID_CHARACTERS_PATTERN = Pattern.compile("[<>:\\\"\\\\\\\\|?*]");

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        return !NOT_VALID_CHARACTERS_PATTERN.matcher(path).find();
    }
}
