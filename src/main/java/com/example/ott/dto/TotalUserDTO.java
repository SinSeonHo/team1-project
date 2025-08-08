package com.example.ott.dto;

import com.example.ott.customValidation.BlankChecks;
import com.example.ott.customValidation.FormatChecks;
import com.example.ott.customValidation.PasswordMatches;
import com.example.ott.customValidation.UniqueNickname;
import com.example.ott.type.Gender;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@AllArgsConstructor
@Builder
@ToString

@PasswordMatches
@UniqueNickname
public class TotalUserDTO {

    @NotBlank(message = "이름을 입력해주세요.", groups = BlankChecks.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "특수문자는 사용할 수 없습니다.", groups = FormatChecks.class)
    @Size(min = 2, message = "이름은 최소 2자 이상이어야 합니다.", groups = FormatChecks.class)
    private String name;

    @NotBlank(message = "닉네임을 입력해주세요.", groups = BlankChecks.class)
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "특수문자는 사용할 수 없습니다.", groups = FormatChecks.class)
    @Size(min = 2, max = 12, message = "닉네임의 길이는 최소 2글자, 최대 12글자 입니다.", groups = FormatChecks.class)
    private String nickname;

    @NotBlank(message = "아이디를 입력해주세요.", groups = BlankChecks.class)
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "특수문자는 사용할 수 없습니다.", groups = FormatChecks.class)
    @Size(min = 3, max = 20, message = "아이디는 3~20자여야 합니다.", groups = FormatChecks.class)
    private String id;

    @NotBlank(message = "비밀번호를 입력해주세요.", groups = BlankChecks.class)
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.", groups = FormatChecks.class)
    private String password;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.", groups = BlankChecks.class)
    private String checkPassword;

    @NotNull(message = "나이를 입력해주세요.", groups = BlankChecks.class)
    @Min(value = 1, message = "나이는 1살 이상이어야 합니다.", groups = FormatChecks.class)
    @Max(value = 100, message = "나이는 100살 이하여야 합니다.", groups = FormatChecks.class)
    private Integer age;

    @NotNull(message = "성별은 필수 선택값입니다.", groups = BlankChecks.class)
    private Gender gender;

}
