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

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.entity.User;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

public class CustomUserDetails implements UserDetails, OAuth2User {


    // User Entity 정보
    private SecurityUserDTO securityUserDTO;
    
    // 소셜 로그인시 해당 소셜에서 제공해주는 모든 정보
    private Map<String, Object> attributes;

    // 일반 유저 생성자
    public CustomUserDetails(User user) {
        this.securityUserDTO = SecurityUserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
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
        .nickname(user.getNickname())
        .socials(user.getSocials())
        .userRole(user.getUserRole())
        .build();
        this.attributes = attributes;
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
        return securityUserDTO.getNickname();
    }

    @Override
    // 계정의 권한 목록(USER, MANAGER, ADMIN)을 리턴
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = this.securityUserDTO.getUserRole().getAuthorities();

        return authorities;
    }

}
