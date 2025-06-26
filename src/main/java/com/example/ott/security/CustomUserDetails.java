package com.example.ott.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.entity.User;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

public class CustomUserDetails implements UserDetails, OAuth2User {

    // User Entity 정보
    private SecurityUserDTO securityUserDTO;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    // 소셜 로그인시 해당 소셜에서 제공해주는 모든 정보
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
                .socials(user.getSocials())
                .userRole(user.getUserRole())
                .build();

    }

    // 소셜 로그인 유저 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        System.out.println("Social user 정보 " + user);
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

    // 최초 로그인 판별용
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