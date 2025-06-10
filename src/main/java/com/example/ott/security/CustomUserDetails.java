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

import com.example.ott.entity.User;

public class CustomUserDetails implements UserDetails, OAuth2User {

    // User Entity 정보
    private User user;
    
    // 소셜 로그인시 해당 소셜에서 제공해주는 모든 정보
    private Map<String, Object> attributes;

    // 일반 유저 생성자
    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 소셜 로그인 유저 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

 

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId();
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
        List<GrantedAuthority> authorities = this.user.getUserRole().getAuthorities();

        return authorities;
    }

}
