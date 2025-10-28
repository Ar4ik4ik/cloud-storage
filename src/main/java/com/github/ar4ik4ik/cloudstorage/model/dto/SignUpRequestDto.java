package com.github.ar4ik4ik.cloudstorage.model.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record SignUpRequestDto(@NotEmpty @Min(3) String username,
                               @NotEmpty @Min(8) String password) {
}
