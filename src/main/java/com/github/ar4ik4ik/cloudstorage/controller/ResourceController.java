package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    @GetMapping
    public ResponseEntity<?> getResourceInfo(@RequestParam(name = "path") String resourcePath) {
        return null;
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") String resourcePath) {
        storageService.deleteResource(resourcePath);
        return ResponseEntity
                .noContent().build();
    }

    @GetMapping(path = "download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam(name = "path") String resourcePath) {
        String filename = PathUtils.getFilenameForDownload(resourcePath);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''%s".formatted(filename))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(storageService.downloadResource(resourcePath));
    }

    @GetMapping(path = "move")
    public ResponseEntity<ResourceInfoResponseDto> moveResource(@RequestParam(name = "from") String moveFrom,
                                          @RequestParam(name = "to") String moveTo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(storageService.moveResource(moveFrom, moveTo));

    }

    @GetMapping(path = "search")
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
