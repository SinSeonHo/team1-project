package com.example.ott.security;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.entity.User;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private SecurityUserDTO securityUserDTO;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Map<String, Object> attributes;

    // 일반 유저 생성자
    public CustomUserDetails(User user) {
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();

        this.securityUserDTO = SecurityUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .password(user.getPassword())
                // .image(user.getImage())
                .socials(user.getSocials())
                .userRole(user.getUserRole())
                .build();

    }

    // 소셜 로그인 유저 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.securityUserDTO = SecurityUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .socials(user.getSocials())
                .userRole(user.getUserRole())
                .build();

        this.attributes = attributes;
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
    }

    @Override
    public String getPassword() {
        return securityUserDTO.getPassword();
    }

    @Override
    public String getUsername() {
        return securityUserDTO.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    // 계정의 권한 목록(USER, MANAGER, ADMIN)을 리턴
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = this.securityUserDTO.getUserRole().getAuthorities();

        return authorities;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
}