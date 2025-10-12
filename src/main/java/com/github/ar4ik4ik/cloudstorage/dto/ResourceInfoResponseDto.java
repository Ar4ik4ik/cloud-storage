package com.github.ar4ik4ik.cloudstorage.dto;

import lombok.Builder;

@Builder
public record ResourceInfoResponseDto(String path, String name, Long size, String type) {
    public enum ResourceType {
        FILE, DIRECTORY;
    }
}
