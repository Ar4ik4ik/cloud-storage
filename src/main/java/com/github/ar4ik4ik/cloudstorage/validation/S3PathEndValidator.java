package com.github.ar4ik4ik.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class S3PathEndValidator implements ConstraintValidator<ValidS3PathEnd, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null) {
            return true;
        }

        return path.isEmpty() || path.endsWith("/");
    }
}
