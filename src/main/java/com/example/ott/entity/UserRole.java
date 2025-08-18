package com.example.ott.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    GUEST("GUEST"),
    USER("USER"),
    MANAGER("USER,MANAGER"),
    ADMIN("USER,MANAGER,ADMIN"),
    PENDING("PENDING"),
    BAN("BAN");

    private final String roles;

    UserRole(String roles) {
        this.roles = roles;
    }

    // CustomUserDetails에서 권한 불러올 때 사용
    public List<GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    public boolean isPending() {
        return this == PENDING;
    }
}
