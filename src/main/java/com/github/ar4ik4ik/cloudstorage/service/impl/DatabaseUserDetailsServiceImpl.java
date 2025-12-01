package com.github.ar4ik4ik.cloudstorage.service.impl;


import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.StorageUserDetails;
import com.github.ar4ik4ik.cloudstorage.model.entity.Authority;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username)
                .map(this::buildStorageUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with username: %s not found".formatted(username)));
    }

    private StorageUserDetails buildStorageUserDetails(com.github.ar4ik4ik.cloudstorage.model.entity.User userEntity) {
        return StorageUserDetails.builder()
                .user(buildSpringSecurityUser(userEntity))
                .userRootDirectory(buildUserRootDirectory(userEntity.getId()))
                .build();
    }

    private User buildSpringSecurityUser(com.github.ar4ik4ik.cloudstorage.model.entity.User userEntity) {
        List<SimpleGrantedAuthority> grantedAuthorities = mapAuthoritiesToGrantedAuthorities(userEntity.getAuthorities());
        return new org.springframework.security.core.userdetails.User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                grantedAuthorities
        );
    }

    private List<SimpleGrantedAuthority> mapAuthoritiesToGrantedAuthorities(Collection<Authority> authorities) {
        return authorities.stream()
                .map(Authority::getName)
                .map(AuthorityType::getAuthority)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private String buildUserRootDirectory(Integer userId) {
        return String.format("user-%s-files", userId);
    }
}
