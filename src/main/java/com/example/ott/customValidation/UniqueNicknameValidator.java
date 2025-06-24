package com.example.ott.customValidation;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.ott.dto.UserProfileDTO;
import com.example.ott.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueNicknameValidator implements ConstraintValidator<UniqueNickname, UserProfileDTO> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(UserProfileDTO dto, ConstraintValidatorContext context) {
        String id = dto.getId();
        String nickname = dto.getNickname();
        if (nickname == null || nickname.isBlank())
            return true;

        if (nickname == null || nickname.isBlank())
            return true;

        // 닉네임 중복 검사 (본인 아이디 제외)
        boolean isUnique = !userRepository.existsByNicknameAndIdNot(nickname, id);

        if (!isUnique) {
            // nickname 필드에 메시지를 바인딩
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("nickname")
                    .addConstraintViolation();
        }
        return isUnique;
    }
}
