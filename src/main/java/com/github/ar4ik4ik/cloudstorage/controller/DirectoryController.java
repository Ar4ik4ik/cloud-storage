package com.github.ar4ik4ik.cloudstorage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directory")
public class DirectoryController {

    @GetMapping(params = "path")
    public ResponseEntity<?> getDirectoryInfo(@RequestParam(name = "path") String directoryPath) {
        return null;
    }

    @PostMapping(params = "path")
    public ResponseEntity<?> createDirectory(@RequestParam(name = "path") String directoryPath) {
        return null;
    }
}
