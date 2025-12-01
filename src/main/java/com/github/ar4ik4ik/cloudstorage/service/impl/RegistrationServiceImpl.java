package com.github.ar4ik4ik.cloudstorage.service.impl;


import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.entity.User;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    @Transactional
    public void registerUser(SignUpRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.username())) {
            throw new UserAlreadyExistsException(String.format("Username %s already exists", requestDto.username()));
        }

        var savedUser = userRepository.save(createUserEntity(requestDto));

        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser.getId()));
        log.info("Published user registered event");
    }

    @NotNull
    private User createUserEntity(SignUpRequestDto requestDto) {
        return User.builder()
                .username(requestDto.username().toLowerCase())
                .password(passwordEncoder.encode(requestDto.password()))
                .authorities(Collections.singletonList(
                        authorityRepository.getAuthorityByName(AuthorityType.ROLE_USER)))
                .build();
    }
}
