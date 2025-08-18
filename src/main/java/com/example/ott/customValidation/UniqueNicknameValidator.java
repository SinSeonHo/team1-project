package com.example.ott.customValidation;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.ott.dto.TotalUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UniqueNicknameValidator implements ConstraintValidator<UniqueNickname, Object> {

    private final UserRepository userRepository;

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null)
            return true; // 대상 없음 → 통과

        String id = null;
        String nickname = null;
        boolean exists;

        if (value instanceof UserProfileDTO dto) {
            // 프로필 수정
            id = dto.getId();
            nickname = normalize(dto.getNickname());
            if (nickname == null)
                return true;

            // id가 null일 수도 있으니 안전하게 처리
            exists = (id == null)
                    ? userRepository.existsByNickname(nickname)
                    : userRepository.existsByNicknameAndIdNot(nickname, id);

        } else if (value instanceof TotalUserDTO dto) {
            // 회원가입
            nickname = normalize(dto.getNickname());
            if (nickname == null)
                return true;

            exists = userRepository.existsByNickname(nickname);

        } else {
            // 지원하지 않는 타입 → 검증 대상 아님
            return true;
        }

        if (exists) {
            bindFieldError(context, "nickname");
            return false; // 중복이면 유효하지 않음
        }
        return true; // 중복 없으면 통과
    }

    // 공백 제거, 빈문자열 체크, 소문자 통일
    private String normalize(String s) {
        if (s == null)
            return null;

        // 앞뒤 공백 제거
        String t = s.trim();

        // 빈 문자열이면 null 반환
        if (t.isEmpty())
            return null;

        // 모두 소문자로 변환
        return t.toLowerCase(Locale.ROOT);
    }

    private void bindFieldError(ConstraintValidatorContext ctx, String field) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(ctx.getDefaultConstraintMessageTemplate())
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}