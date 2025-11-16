package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository;


    @Override
    public void authenticate(SignInRequestDto requestDto) {
        var authToken = new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password());
        var authRequest = authenticationManager.authenticate(authToken);

        SecurityContextHolder.getContext().setAuthentication(authRequest);
    }

    @Override
    public void authenticateDirectly(SignInRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password());
        var authRequest = authenticationManager.authenticate(authToken);

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authRequest);
        securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);
    }
}
