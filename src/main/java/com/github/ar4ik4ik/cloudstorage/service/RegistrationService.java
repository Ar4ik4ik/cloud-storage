package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;

public interface RegistrationService {
    void registerUser(SignUpRequestDto requestDto);
}
