package com.example.ott.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.ott.entity.User;

public class CustomUserDetails implements UserDetails {

    // User Entity 를 통째로 불러오기
    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    // 계정의 권한 목록을 리턴?
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = this.user.getUserRole().getAuthorities();

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId();
    }

}
