package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.TestcontainersConfiguration;
import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.entity.User;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class RegistrationServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private HttpServletRequest servletRequest;

    @MockitoBean
    private HttpServletResponse servletResponse;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private RegistrationServiceImpl registrationService;

    private SignUpRequestDto signUpRequestDto;

    @BeforeEach
    void setUp() {
        signUpRequestDto = new SignUpRequestDto("testuser", "Password123");
        when(passwordEncoder.encode(signUpRequestDto.password())).thenReturn("encodedPassword123");
    }

    @Test
    @DisplayName("Успешная регистрация с корректными данными сохраняет нового пользователя в БД")
    void registerUser_ShouldPersistUser() {
        // when
        registrationService.registerUser(signUpRequestDto);
        // then
        User newUser = userRepository.findUserByUsername(signUpRequestDto.username().toLowerCase()).orElse(null);
        assertThat(newUser).isNotNull();
        assertThat(newUser.getAuthorities()).hasSize(1);
        assertThat(newUser.getAuthorities().getFirst().getName()).isEqualTo(AuthorityType.ROLE_USER);
        assertThat(newUser.getPassword()).isEqualTo("encoded".concat(signUpRequestDto.password()));
    }

    @Test
    @DisplayName("Ошибка при регистрации пользователя с неуникальным логином")
    void registerUser_ShouldThrowUserAlreadyExistsException() {
        // when
        registrationService.registerUser(signUpRequestDto);
        // then
        assertThatThrownBy(() -> registrationService.registerUser(signUpRequestDto))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @Disabled
    void registerUser_ShouldDirectlyAuthUser() {
        // when
        registrationService.registerUser(signUpRequestDto);
        // then
        verify(authenticationService, times(1))
                .authenticateDirectly(any(), any(), any());
    }


    @Test
    @Disabled
    void registerUser_ShouldPublishUserRegisteredEvent() {
        // when
        registrationService.registerUser(signUpRequestDto);
        // then
        verify(eventPublisher, times(1)).publishEvent(any(UserRegisteredEvent.class));
        verifyNoMoreInteractions(eventPublisher);
    }
}