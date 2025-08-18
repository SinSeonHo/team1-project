package com.example.ott.customValidation;

import com.example.ott.dto.TotalUserDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, TotalUserDTO> {
    @Override
    public boolean isValid(TotalUserDTO dto, ConstraintValidatorContext ctx) {
        if (dto == null)
            return true;

        String p1 = dto.getPassword();
        String p2 = dto.getCheckPassword();

        // 비어있음은 각 필드의 @NotBlank/@Size가 처리하게 여기서는 통과
        if (p1 == null || p2 == null)
            return true;

        boolean matches = p1.equals(p2);
        if (!matches) {
            // 기본(object-level) 메시지 비활성화 후, checkPassword 필드로 귀속
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                    .addPropertyNode("checkPassword")
                    .addConstraintViolation();
        }
        return matches;
    }
}
