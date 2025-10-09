package com.github.ar4ik4ik.cloudstorage.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/resource")
@RestController
public class ResourceController {

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
    public ResponseEntity<?> downloadResource() {
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

    @PostMapping(params = {"path", "file"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResource(@RequestParam(name = "path") String resourcePath,
                                            @RequestPart(name = "file") MultipartFile multipartFile) {
        return null;
    }
}
