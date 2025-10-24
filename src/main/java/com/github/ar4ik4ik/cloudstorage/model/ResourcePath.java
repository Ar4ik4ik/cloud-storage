package com.github.ar4ik4ik.cloudstorage.model;

import com.github.ar4ik4ik.cloudstorage.validation.ValidCharacters;
import jakarta.validation.constraints.NotBlank;

public record ResourcePath(@NotBlank @ValidCharacters String path) {
}
