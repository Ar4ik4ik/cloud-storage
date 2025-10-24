package com.github.ar4ik4ik.cloudstorage.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<ValidFiles, MultipartFile[]> {

    @Override
    public boolean isValid(MultipartFile[] files, ConstraintValidatorContext constraintValidatorContext) {
        for (MultipartFile file: files) {
            if (file == null) {
                return false;
            }
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
                return false;
            }
        }
        return true;
    }
}
