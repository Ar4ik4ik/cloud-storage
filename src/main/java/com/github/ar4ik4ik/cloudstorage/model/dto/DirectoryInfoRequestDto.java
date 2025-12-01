package com.github.ar4ik4ik.cloudstorage.model.dto;

import com.github.ar4ik4ik.cloudstorage.validation.ValidS3PathEnd;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DirectoryInfoRequestDto(
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9а-яА-ЯёЁ!\\-_.*'()/ ]*$",
                message = "Path contains invalid characters. Only alphanumeric, !, -, _, ., *, ', (, ), and / are allowed.")
        @ValidS3PathEnd(message = "Directory path must be empty or ends with /")
        String path
        ) {
}
