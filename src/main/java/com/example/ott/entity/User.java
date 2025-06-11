package com.example.ott.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

@Entity
@Table(name="user_table")
public class User {
    
    // TODO : UserCode 생성 기능 추가 필요
    @Id
    private String id;

    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Socials socials = Socials.NONE; // 소셜 계정(Kakao, Naver, Google, X)

    @Builder.Default
    private Long mileage = 0L;

    // private Struct struct;
}
