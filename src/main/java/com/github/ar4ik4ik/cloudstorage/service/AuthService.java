package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void registerUser(SignUpRequestDto requestDto, HttpServletRequest request, HttpServletResponse response);
    void authenticateUser(SignInRequestDto requestDto);
}
