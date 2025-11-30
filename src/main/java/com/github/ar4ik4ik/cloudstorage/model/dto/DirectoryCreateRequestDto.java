package com.github.ar4ik4ik.cloudstorage.model.dto;

import com.github.ar4ik4ik.cloudstorage.validation.ValidS3PathEnd;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record DirectoryCreateRequestDto(
        @NotEmpty(message = "Directory path can't be empty")
        @Pattern(regexp = "^[a-zA-Z0-9!\\-_.*'()/]*$",
                message = "Path contains invalid characters. Only alphanumeric, !, -, _, ., *, ', (, ), and / are allowed.")
        @ValidS3PathEnd(message = "Directory path must be empty or ends with /")
        String path
        ) {
}
