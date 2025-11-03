package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.AuthResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getUser(Authentication authentication) {
        return ResponseEntity.ok((new AuthResponseDto(authentication.getName())));
    }
}