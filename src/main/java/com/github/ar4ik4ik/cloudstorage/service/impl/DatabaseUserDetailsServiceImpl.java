package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.controller.UserCreateDto;
import com.github.ar4ik4ik.cloudstorage.model.entity.Authority;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsServiceImpl implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findUserByUsername(username)
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getAuthorities().stream()
                                .map(Authority::getName)
                                .map(AuthorityType::getAuthority)
                                .map(SimpleGrantedAuthority::new)
                                .toList())
                        .build()).orElseThrow(() -> new UsernameNotFoundException(
                        "User with username: %s not found".formatted(username)));
    }

    @Transactional
    public void processUserRegister(UserCreateDto userCreateDto) {
        this.userRepository.save(com.github.ar4ik4ik.cloudstorage.model.entity.User.builder()
                        .username(userCreateDto.username())
                        .password(passwordEncoder.encode(userCreateDto.password()))
                        .authorities(List.of(
                                authorityRepository.getAuthorityByName(AuthorityType.ROLE_USER)))
                .build());
    }
}
