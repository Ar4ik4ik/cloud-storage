package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.AuthResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.service.impl.LoginAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    @PostMapping(path = "/sign-in")
    public ResponseEntity<AuthResponseDto> signIn(@Valid @RequestBody SignInRequestDto requestDto) {
        service.authenticateUser(requestDto);
        return ResponseEntity.ok(new AuthResponseDto(requestDto.username()));
    }

    @PostMapping(path = "/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody SignUpRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        service.registerUser(requestDto, request, response);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponseDto(requestDto.username()));
    }

    @PostMapping(path = "/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.noContent().build();
    }

}
