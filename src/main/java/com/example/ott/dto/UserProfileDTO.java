package com.example.ott.dto;

import java.time.LocalDateTime;

import com.example.ott.customValidation.UniqueNickname;
import com.example.ott.entity.Image;
import com.example.ott.entity.Socials;
import com.example.ott.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

@UniqueNickname(message = "이미 존재하는 닉네임입니다.")
public class UserProfileDTO {

    private String id;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private String email;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname; // 별명

    private Socials socials; // 소셜 계정(Kakao, Naver, Google, X)

    private Long mileage;

    private String Genres;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String grade;

    private String profileImageUrl;

    // private Grade grade? : 마일리지 등급에 따라 레벨 같은 거 꾸며주기(뱃지)
}
