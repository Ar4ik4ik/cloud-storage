package com.github.ar4ik4ik.cloudstorage.service;

import com.github.ar4ik4ik.cloudstorage.controller.UserCreateDto;
import com.github.ar4ik4ik.cloudstorage.model.entity.Authority;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import com.github.ar4ik4ik.cloudstorage.model.entity.User;
import com.github.ar4ik4ik.cloudstorage.repository.AuthorityRepository;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.impl.DatabaseUserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class DatabaseUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorityRepository authorityRepository;

    @InjectMocks
    private DatabaseUserDetailsServiceImpl databaseUserDetailsServiceImpl;

    @Test
    @DisplayName("Returns userDetails object when username is exists")
    void loadUserByUserName_ReturnsUserDetails() {

        // given
        String username = "testUser";
        User user = User.builder()
                .authorities(List.of(Authority.builder()
                                .name(AuthorityType.ROLE_USER)
                                .id(1)
                        .build()))
                .password("testPassword")
                .enabled(true)
                .createdAt(OffsetDateTime.now())
                .username(username)
                .build();

        // when
        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        UserDetails userDetails = databaseUserDetailsServiceImpl.loadUserByUsername(username);

        // then
        var expectedAuthorities = List.of(new SimpleGrantedAuthority(AuthorityType.ROLE_USER.getAuthority()));

        assertArrayEquals(expectedAuthorities.toArray(), userDetails.getAuthorities().toArray());
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(user.getEnabled(), userDetails.isEnabled());
        verify(userRepository).findUserByUsername(username);
    }

    @Test
    @DisplayName("Throws exception when user is not exists")
    void loadUserByUserName_ThrowsUsernameNotFoundException() {
        // given
        String notExistingUsername = "notExistingUsername";

        // when
        when(userRepository.findUserByUsername(notExistingUsername)).thenReturn(Optional.empty());

        // then
        assertThrows(UsernameNotFoundException.class,
                () -> databaseUserDetailsServiceImpl.loadUserByUsername(notExistingUsername));

        verify(userRepository).findUserByUsername(notExistingUsername);
    }

    @Test
    @DisplayName("Process user register")
    void processUserRegister_ReturnsVoid() {
        // given
        String username = "testUser";
        String rawPassword = "testPassword";
        String encodedPassword = "encodedTestPassword";

        UserCreateDto userCreateDto = new UserCreateDto(username, rawPassword);

        Authority authority = Authority.builder()
                .id(1)
                .name(AuthorityType.ROLE_USER)
                .build();

        when(passwordEncoder.encode(eq(rawPassword))).thenReturn(encodedPassword);
        when(authorityRepository.getAuthorityByName(eq(AuthorityType.ROLE_USER))).thenReturn(authority);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        databaseUserDetailsServiceImpl.processUserRegister(userCreateDto);

        // then
        verify(passwordEncoder, times(1)).encode(eq(rawPassword));
        verify(authorityRepository, times(1)).getAuthorityByName(eq(AuthorityType.ROLE_USER));

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(argumentCaptor.capture());

        User user = argumentCaptor.getValue();

        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(encodedPassword, user.getPassword());
        assertTrue(user.getEnabled());

        assertNotNull(user.getAuthorities());
        assertEquals(1, user.getAuthorities().size());
        assertEquals(authority, user.getAuthorities().getFirst());


    }

}
