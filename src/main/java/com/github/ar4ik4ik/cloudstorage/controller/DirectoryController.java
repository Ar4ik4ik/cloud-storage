package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directory")
@Validated
@Slf4j
public class DirectoryController {

    private final StorageService service;

    @GetMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryInfo(@RequestParam(name = "path")
                                                                              ResourcePath path) {
        log.info("DirectoryController.getDirectoryInfo called. Resolved path: '{}'", path.path());
        return ResponseEntity.ok(service.getDirectoryInfo(path.path()));
    }

    @PostMapping
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(@RequestParam(name = "path") @Valid ResourcePath path) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/api/directory")
                .queryParam("path", path.path())
                .build().toUri();
        return ResponseEntity.created(location)
                .body(service.createDirectory(path.path()));
    }
}
