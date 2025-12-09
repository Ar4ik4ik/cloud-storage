package com.github.ar4ik4ik.cloudstorage.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResourcePathRequestDto(
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9а-яА-ЯёЁ !\\-_\\.'\\(\\)\\/]*$",
                message = "Path contains invalid characters. Only alphanumeric (Latin & Cyrillic), space, !, -, _, ., ', (, ), and / are allowed.")
        @Size(max = 100)
        String path) {
}
