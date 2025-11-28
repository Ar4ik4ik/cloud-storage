package com.github.ar4ik4ik.cloudstorage.service.impl;


import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.AuthenticationService;
import com.github.ar4ik4ik.cloudstorage.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationService authenticationService;

    @Transactional
    public void registerUser(SignUpRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        if (userRepository.existsByUsername(requestDto.username())) {
            throw new UserAlreadyExistsException(String.format("Username %s already exists", requestDto.username()));
        }

        var savedUser = userRepository.save(com.github.ar4ik4ik.cloudstorage.model.entity.User.builder()
                .username(requestDto.username().toLowerCase())
                .password(passwordEncoder.encode(requestDto.password()))
                .authorities(Collections.singletonList(
                        authorityRepository.getAuthorityByName(AuthorityType.ROLE_USER)))
                .build());

        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser.getId()));
        log.info("Published event");


        authenticationService.authenticateDirectly(new SignInRequestDto(requestDto.username(), requestDto.password()),
                request, response);
    }
}
