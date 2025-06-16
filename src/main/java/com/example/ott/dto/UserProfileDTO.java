package com.example.ott.dto;

import java.time.LocalDateTime;

import com.example.ott.entity.Socials;
import com.example.ott.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserProfileDTO {

    private String id;

    private String name;

    private String email;

    private String nickname; // 별명

    private Socials socials; // 소셜 계정(Kakao, Naver, Google, X)

    private Long mileage;

    private String Genres;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    // private Grade grade? : 마일리지 등급에 따라 레벨 같은 거 꾸며주기(뱃지)
}
