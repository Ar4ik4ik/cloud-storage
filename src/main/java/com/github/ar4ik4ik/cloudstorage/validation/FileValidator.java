package com.github.ar4ik4ik.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

public class FileValidator implements ConstraintValidator<ValidFiles, MultipartFile[]> {

    private final static Pattern ALLOWED_SYMBOLS_REGEX = Pattern.compile("^[a-zA-Z0-9а-яА-ЯёЁ !\\-_\\.'\\(\\)\\/]*$");

    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext constraintValidatorContext) {
        if (files == null || files.length == 0) {
            return false;
        }
        for (MultipartFile file: files) {
            if (file == null) {
                return false;
            }
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
                return false;
            }
            if (!ALLOWED_SYMBOLS_REGEX.matcher(file.getOriginalFilename()).matches()) {
                return false;
            }
        }
        return true;
    }
}
