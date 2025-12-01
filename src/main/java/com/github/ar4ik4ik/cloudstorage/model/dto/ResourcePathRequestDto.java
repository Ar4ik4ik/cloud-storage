package com.github.ar4ik4ik.cloudstorage.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ResourcePathRequestDto(
        @NotNull
        @Pattern(
                regexp = "^[a-zA-Z0-9а-яА-ЯёЁ!\\-_.*'()/ ]*$",
                message = "Path contains invalid characters. Only alphanumeric, !, -, _, ., *, ', (, ), and / are allowed."
        )
        String path) {
}
