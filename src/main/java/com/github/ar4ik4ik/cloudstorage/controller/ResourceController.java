package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.model.StorageUserDetails;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import com.github.ar4ik4ik.cloudstorage.validation.ValidFiles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.getFullPathFromRootAndDestination;

@RequestMapping("/api/resource")
@RestController
@RequiredArgsConstructor
@Validated
public class ResourceController {

    private final StorageServiceImpl service;

    @GetMapping
    public ResponseEntity<ResourceInfoResponseDto> getResourceInfo(@RequestParam(name = "path") @Valid ResourcePath path, @AuthenticationPrincipal StorageUserDetails userDetails) {
        return ResponseEntity.ok(service.getResourceInfo(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path()))));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") @Valid ResourcePath path, @AuthenticationPrincipal StorageUserDetails userDetails) {
        service.deleteResource(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "download")
    public ResponseEntity<StreamingResponseBody> downloadResource(@RequestParam(name = "path") @Valid ResourcePath path, @AuthenticationPrincipal StorageUserDetails userDetails) {
        String filename = PathUtils.getFilenameForDownload(path.path());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''%s".formatted(filename))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.downloadResource(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path())));
    }

    @GetMapping(path = "move")
    public ResponseEntity<ResourceInfoResponseDto> moveResource(@RequestParam(name = "from") @Valid ResourcePath sourcePath,
                                                                @RequestParam(name = "to") @Valid ResourcePath targetPath, @AuthenticationPrincipal StorageUserDetails userDetails) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.moveResource(getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), sourcePath.path()),
                        getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), targetPath.path())));
    }

    @GetMapping(path = "search")
    public ResponseEntity<?> searchResource(@RequestParam(name = "query") @Valid ResourcePath searchQuery,
                                            @AuthenticationPrincipal StorageUserDetails userDetails) {
        return ResponseEntity.ok(service.searchResourcesByQuery(searchQuery.path(), userDetails.getUserRootDirectory()));
    }

    @PostMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> uploadResource(
            @RequestParam(name = "path") @Valid ResourcePath path,
            @RequestParam(name = "object") @ValidFiles MultipartFile[] files, @AuthenticationPrincipal StorageUserDetails userDetails) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/api/resource")
                .queryParam("path", path.path())
                .build().toUri();
        return ResponseEntity.created(location)
                .body(service.uploadResource(files, getFullPathFromRootAndDestination(userDetails.getUserRootDirectory(), path.path())));
    }
}
