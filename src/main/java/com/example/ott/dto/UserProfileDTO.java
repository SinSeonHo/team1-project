package com.example.ott.dto;

import java.time.LocalDateTime;

import com.example.ott.customValidation.BlankChecks;
import com.example.ott.customValidation.FormatChecks;
import com.example.ott.customValidation.UniqueNickname;

import com.example.ott.entity.Socials;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter

@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString

@UniqueNickname
public class UserProfileDTO {

    private String id;

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private String email;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "특수문자는 사용할 수 없습니다.")
    @Size(min = 2, max = 12, message = "닉네임의 길이는 최소 2글자, 최대 12글자 입니다.")
    private String nickname;

    private Socials socials;

    private Long mileage;

    private String Genres;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String grade;

    private String profileImageUrl;
}
