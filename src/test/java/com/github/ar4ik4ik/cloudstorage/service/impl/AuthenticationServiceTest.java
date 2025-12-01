package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.TestcontainersConfiguration;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private HttpServletRequest mockRequest;

    @MockitoBean
    private HttpServletResponse mockResponse;

    private SignInRequestDto validRequestDto;
    private SignInRequestDto invalidRequestDto;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        validRequestDto = new SignInRequestDto("testuser", "password123");
        invalidRequestDto = new SignInRequestDto("wronguser", "wrongpass");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getName()).thenReturn(validRequestDto.username());

        when(authenticationManager.authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getName().equals(validRequestDto.username()) &&
                        auth.getCredentials().equals(validRequestDto.password())))
        ).thenReturn(mockAuthentication);

        when(authenticationManager.authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getName().equals(invalidRequestDto.username()) &&
                        auth.getCredentials().equals(invalidRequestDto.password())))
        ).thenThrow(new BadCredentialsException("Bad credentials"));
    }

    @Test
    @DisplayName("Успешная аутентификация с корректным логпасом приводит к установке контекста в Spring Security")
    void authenticate_ShouldSetSecurityContextForValidCredentials() {
        // when
        authenticationService.authenticate(validRequestDto);
        // then
        var context = SecurityContextHolder.getContext();
        assertThat(context).isNotNull();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().isAuthenticated()).isTrue();
        assertThat(context.getAuthentication().getName()).isEqualTo(validRequestDto.username());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Ошибка при аутентификации с неверными учетными данными")
    void authenticate_ShouldThrowsExceptionForInvalidCredentials() {
        // when
        assertThatThrownBy(() -> authenticationService.authenticate(invalidRequestDto))
                .isInstanceOf(BadCredentialsException.class);
        // then
        var context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoMoreInteractions(authenticationManager);
    }

    @Test
    @DisplayName("Успешная аутентификация с корректными учетными данными напрямую устанавливает и сохраняет контекст в Spring Security")
    void authenticateDirectly_ShouldSetSecurityContextForValidCredentialsAndSaveIt() {
        // when
        authenticationService.authenticateDirectly(validRequestDto, mockRequest, mockResponse);
        // then
        var context = SecurityContextHolder.getContext();
        assertThat(context).isNotNull();
        assertThat(context.getAuthentication()).isNotNull();
        assertThat(context.getAuthentication().isAuthenticated()).isTrue();
        assertThat(context.getAuthentication().getName()).isEqualTo(validRequestDto.username());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(securityContextRepository, times(1)).saveContext(
                argThat(ctx -> ctx.getAuthentication() != null && ctx.getAuthentication().isAuthenticated()),
                eq(mockRequest),
                eq(mockResponse));

        verifyNoMoreInteractions(authenticationManager, securityContextRepository);
    }
}