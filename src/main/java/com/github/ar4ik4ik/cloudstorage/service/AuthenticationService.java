package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    void authenticate(SignInRequestDto requestDto);

    void authenticateDirectly(SignInRequestDto signInRequestDto, HttpServletRequest request, HttpServletResponse response);
}
