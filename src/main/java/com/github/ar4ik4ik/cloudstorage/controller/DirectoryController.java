package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.model.StorageUserDetails;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.getFullPathFromRootAndDestination;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directory")
@Validated
public class DirectoryController {

    private final StorageService service;

    @GetMapping(params = "path")
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryInfo(@RequestParam(name = "path") @Valid ResourcePath path, @AuthenticationPrincipal StorageUserDetails userDetails) {
        return ResponseEntity.ok(service.getDirectoryInfo(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path())));
    }

    @PostMapping(params = "path")
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(@RequestParam(name = "path") @Valid ResourcePath path, @AuthenticationPrincipal StorageUserDetails userDetails) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/api/directory")
                .queryParam("path", path.path())
                .build().toUri();
        return ResponseEntity.created(location)
                .body(service.createDirectory(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path())));
    }
}
