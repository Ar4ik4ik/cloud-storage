package com.github.ar4ik4ik.cloudstorage.model.dto;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record SignInRequestDto(@NotEmpty @Size(min = 3, max = 50) String username,
                               @NotEmpty @Size(min = 8, max = 100) String password) {
}
