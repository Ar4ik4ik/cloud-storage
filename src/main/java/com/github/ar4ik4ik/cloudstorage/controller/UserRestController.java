package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.entity.User;
import com.github.ar4ik4ik.cloudstorage.repository.UserRepository;
import com.github.ar4ik4ik.cloudstorage.service.impl.DatabaseUserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users/")
public class UserRestController {

    private final UserRepository userRepository;
    private final DatabaseUserDetailsServiceImpl databaseUserDetailsServiceImpl;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDto userCreateDto) {
        databaseUserDetailsServiceImpl.processUserRegister(userCreateDto);
        return ResponseEntity.noContent().build();
    }
}
