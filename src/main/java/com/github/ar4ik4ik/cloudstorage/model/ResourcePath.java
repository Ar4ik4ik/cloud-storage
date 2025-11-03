package com.github.ar4ik4ik.cloudstorage.model;

import com.github.ar4ik4ik.cloudstorage.validation.ValidCharacters;
import org.jetbrains.annotations.NotNull;

public record ResourcePath(@NotNull @ValidCharacters String path) {
}
