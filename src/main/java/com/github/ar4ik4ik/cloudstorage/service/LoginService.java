package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;

public interface LoginService {
    void processLogin(SignInRequestDto requestDto);
}
