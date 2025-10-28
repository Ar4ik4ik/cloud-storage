package com.github.ar4ik4ik.cloudstorage.service;


import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void registerUser(SignUpRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.username())) {
            throw new UserAlreadyExistsException(String.format("Username %s already exists", requestDto.username()));
        }

        userRepository.save(com.github.ar4ik4ik.cloudstorage.model.entity.User.builder()
                .username(requestDto.username().toLowerCase())
                .password(passwordEncoder.encode(requestDto.password()))
                .authorities(Collections.singletonList(
                        authorityRepository.getAuthorityByName(AuthorityType.ROLE_USER)))
                .build());
    }

    public void authenticateUser(SignInRequestDto requestDto) {
        var authToken = new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password());
        var authRequest = authenticationManager.authenticate(authToken);

        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

}
