package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.service.AuthenticationService;
import com.github.ar4ik4ik.cloudstorage.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AuthenticationService authenticationService;

    @Override
    public void processLogin(SignInRequestDto requestDto) {
        authenticationService.authenticate(requestDto);
    }
}
