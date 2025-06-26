package com.example.ott.dto;

import java.time.LocalDateTime;

import com.example.ott.customValidation.UniqueId;
import com.example.ott.entity.Image;
import com.example.ott.entity.Socials;
import com.example.ott.entity.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class SecurityUserDTO {

    @NotBlank(message = "아이디를 입력해주세요.")
    @UniqueId(message = "이미 존재하는 아이디입니다.")
    private String id;

    @NotBlank(message = "성명을 입력해주세요.")
    private String name;

    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    private UserRole userRole;

    private Socials socials;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
