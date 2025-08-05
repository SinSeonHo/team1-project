package com.example.ott.dto;

import java.time.LocalDateTime;

import com.example.ott.customValidation.UniqueNickname;

import com.example.ott.entity.Socials;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter

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
    private String nickname;

    private Socials socials;

    private Long mileage;

    private String Genres;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String grade;

    private String profileImageUrl;
}
