package com.example.ott.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.ott.dto.UserDTO;
import com.example.ott.entity.User;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

public class CustomUserDetails implements UserDetails, OAuth2User {


    // User Entity 정보
    private UserDTO userDTO;
    
    // 소셜 로그인시 해당 소셜에서 제공해주는 모든 정보
    private Map<String, Object> attributes;

    // 일반 유저 생성자
    public CustomUserDetails(User user) {
        this.userDTO = UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .socials(user.getSocials())
        .userRole(user.getUserRole())
        .mileage(user.getMileage())
        .build();
    }

    // 소셜 로그인 유저 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.userDTO = UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .socials(user.getSocials())
        .userRole(user.getUserRole())
        .mileage(user.getMileage())
        .build();
        this.attributes = attributes;
    }

 

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return userDTO.getId();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    // 잘 사용되지 않음
    @Override
    public String getName() {
        return Strings.EMPTY;
    }

    @Override
    // 계정의 권한 목록(USER, MANAGER, ADMIN)을 리턴
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = this.userDTO.getUserRole().getAuthorities();

        return authorities;
    }

}
