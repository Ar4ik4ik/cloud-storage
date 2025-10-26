package com.github.ar4ik4ik.cloudstorage.repository;

import com.github.ar4ik4ik.cloudstorage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserByUsername(String username);

    boolean existsByUsername(String username);
}
