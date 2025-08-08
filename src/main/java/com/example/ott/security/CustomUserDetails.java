package com.example.ott.security;

import java.time.LocalDateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;

public class CustomUserDetails implements UserDetails, OAuth2User {

    private SecurityUserDTO securityUserDTO;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Map<String, Object> attributes;

    private final TempSocialSignupDTO temp;

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
                .socials(user.getSocial())
                .userRole(user.getUserRole())
                .build();
        this.attributes = null;
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();
        this.temp = null;

    }

    // 소셜 로그인 유저 생성자
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.securityUserDTO = SecurityUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdDate(user.getCreatedDate())
                .updatedDate(user.getUpdatedDate())
                .socials(user.getSocial())
                .userRole(user.getUserRole())
                .build();

        this.attributes = attributes;
        this.createdDate = user.getCreatedDate();
        this.updatedDate = user.getUpdatedDate();

        this.temp = null;
    }

    // 소셜 회원가입 유저 생성자 (아직 DB 저장 안 된 상태)
    public CustomUserDetails(TempSocialSignupDTO tempDTO, Map<String, Object> attributes) {
        this.securityUserDTO = SecurityUserDTO.builder()
                .id(tempDTO.getEmail()) // 이메일을 ID로 임시 설정
                .email(tempDTO.getEmail())
                .name(tempDTO.getName() != null ? tempDTO.getName() : "")
                .nickname(tempDTO.getNickname())
                .socials(tempDTO.getSocial())
                .userRole(UserRole.USER) // 기본 권한 부여
                .build();

        this.attributes = attributes;
        this.createdDate = null; // 아직 저장 전이라 날짜 없음
        this.updatedDate = null; // 아직 저장 전이라 날짜 없음
        this.temp = tempDTO;
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

    public TempSocialSignupDTO getTemp() {
        return temp;
    }

    // ✅ SecurityUserDTO getter 추가 (전역 어드바이스에서 씀)
    public SecurityUserDTO getSecurityUserDTO() {
        return securityUserDTO;
    }

    // ✅ 소셜 회원가입 여부 헬퍼
    public boolean isSocialSignup() {
        return temp != null;
    }

    // ✅ getName() 의미 있는 값 반환 (공백 지양)
    @Override
    public String getName() {
        if (securityUserDTO != null && securityUserDTO.getId() != null)
            return securityUserDTO.getId();
        if (temp != null && temp.getEmail() != null)
            return temp.getEmail();
        return "anonymous";
    }
}