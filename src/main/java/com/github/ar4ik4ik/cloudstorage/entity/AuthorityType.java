package com.github.ar4ik4ik.cloudstorage.entity;

import org.springframework.security.core.GrantedAuthority;

public enum AuthorityType implements GrantedAuthority {

    ROLE_ADMIN, ROLE_USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
