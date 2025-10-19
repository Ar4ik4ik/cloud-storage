package com.github.ar4ik4ik.cloudstorage.repository;

import com.github.ar4ik4ik.cloudstorage.model.entity.Authority;
import com.github.ar4ik4ik.cloudstorage.model.AuthorityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Integer> {

    Authority getAuthorityByName(AuthorityType name);
}
