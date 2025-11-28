package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationServiceImpl authenticationService;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void processLogin_ShouldCallAuthenticateOnAuthenticationService() {
        // given
        var signInRequestDto = new SignInRequestDto("testuser", "testpassword");

        // when
        loginService.processLogin(signInRequestDto);

        // then
        verify(authenticationService, times(1)).authenticate(signInRequestDto);
        verifyNoMoreInteractions(authenticationService);
    }
}