package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    private final StorageService service;

    @GetMapping(params = "path")
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryInfo(@RequestParam(name = "path") @Valid ResourcePath path) {
        return ResponseEntity.ok(service.getDirectoryInfo(path.path()));
    }

    @PostMapping(params = "path")
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(@RequestParam(name = "path") @Valid ResourcePath path) {
        return ResponseEntity.created(URI.create(path.path()))
                .body(service.createDirectory(path.path()));
    }
}
