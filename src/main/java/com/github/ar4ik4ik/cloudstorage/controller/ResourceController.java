package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/resource")
@RestController
@RequiredArgsConstructor
public class ResourceController {

    private final StorageServiceImpl storageService;

    @GetMapping(params = "path")
    public ResponseEntity<?> getResourceInfo(@RequestParam(name = "path") String resourcePath) {
        return null;
    }

    @DeleteMapping(params = "path")
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") String resourcePath) {
        return ResponseEntity
                .noContent().build();
    }

    @GetMapping(path = "download", params = "path", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> downloadResource() {
        return null;
    }

    @GetMapping(path = "move", params = {"from", "to"})
    public ResponseEntity<?> moveResource(@RequestParam(name = "from") String moveFrom,
                                          @RequestParam(name = "to") String moveTo) {
        return null;
    }

    @GetMapping(path = "search", params = "query")
    public ResponseEntity<?> searchResource(@RequestParam(name = "query") String searchQuery) {
        return null;
    }

    @PostMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> uploadResource(
            @RequestParam(name = "path") String resourcePath,
            @RequestParam(name = "files") MultipartFile[] files) {
        return ResponseEntity.created(URI.create(resourcePath))
                .body(storageService.uploadResource(files, resourcePath));
    }
}
