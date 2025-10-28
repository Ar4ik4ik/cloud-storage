package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import com.github.ar4ik4ik.cloudstorage.validation.ValidFiles;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;
import java.util.List;

@RequestMapping("/api/resource")
@RestController
@RequiredArgsConstructor
@Validated
public class ResourceController {

    private final StorageServiceImpl service;

    @GetMapping
    public ResponseEntity<ResourceInfoResponseDto> getResourceInfo(@RequestParam(name = "path") @Valid ResourcePath path) {
        return ResponseEntity.ok(service.getResourceInfo(path.path()));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") @Valid ResourcePath path) {
        service.deleteResource(path.path());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam(name = "path") @Valid ResourcePath path) {
        String filename = PathUtils.getFilenameForDownload(path.path());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''%s".formatted(filename))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.downloadResource(path.path()));
    }

    @GetMapping(path = "move")
    public ResponseEntity<ResourceInfoResponseDto> moveResource(@RequestParam(name = "from") @Valid ResourcePath sourcePath,
                                                                @RequestParam(name = "to") @Valid ResourcePath targetPath) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.moveResource(sourcePath.path(), targetPath.path()));
    }

    @GetMapping(path = "search")
    public ResponseEntity<?> searchResource(@RequestParam(name = "query") @Valid ResourcePath searchQuery) {
        return ResponseEntity.ok(service.searchResourcesByQuery(searchQuery.path()));
    }

    @PostMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> uploadResource(
            @RequestParam(name = "path") @Valid ResourcePath path,
            @RequestParam(name = "files") @NotBlank @ValidFiles MultipartFile[] files) {
        return ResponseEntity.created(URI.create(path.path()))
                .body(service.uploadResource(files, path.path()));
    }
}
