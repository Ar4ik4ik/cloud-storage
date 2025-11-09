package com.github.ar4ik4ik.cloudstorage.service.impl;


import com.github.ar4ik4ik.cloudstorage.event.UserRegisteredEvent;
import com.github.ar4ik4ik.cloudstorage.exception.UserAlreadyExistsException;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class LoginAuthService implements AuthService {
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorityRepository authorityRepository;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

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
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
    }

    public void authenticateUser(SignInRequestDto requestDto) {
        var authToken = new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password());
        var authRequest = authenticationManager.authenticate(authToken);

        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }
}
