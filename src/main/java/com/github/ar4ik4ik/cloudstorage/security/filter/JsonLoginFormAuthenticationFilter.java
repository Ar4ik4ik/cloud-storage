package com.github.ar4ik4ik.cloudstorage.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ar4ik4ik.cloudstorage.model.dto.AuthResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonLoginFormAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final ObjectMapper objectMapper;
    private final SecurityContextRepository securityContextRepository;


    public JsonLoginFormAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper, SecurityContextRepository securityContextRepository) {
        super("/auth/sign-in", authenticationManager);
        this.objectMapper = objectMapper;
        this.securityContextRepository = securityContextRepository;
        setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        SignInRequestDto loginRequest = objectMapper.readValue(request.getInputStream(), SignInRequestDto.class);
        UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username(), loginRequest.password());
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        AuthResponseDto responseDto = new AuthResponseDto(authResult.getName());
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{ \"message\": \"Authentication failed: User doesn't exists or password not match\" }");
    }
}
