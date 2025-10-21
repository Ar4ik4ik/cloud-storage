package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
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
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryInfo(@RequestParam(name = "path") String directoryPath) {
        return ResponseEntity.ok(service.getDirectoryInfo(directoryPath));
    }

    @PostMapping(params = "path")
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(@RequestParam(name = "path") String directoryPath) {
        return ResponseEntity.created(URI.create(directoryPath))
                .body(service.createDirectory(directoryPath));
    }
}
