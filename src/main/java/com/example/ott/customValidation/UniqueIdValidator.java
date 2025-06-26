package com.example.ott.customValidation;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.ott.repository.UserRepository;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueIdValidator implements ConstraintValidator<UniqueId, String> {

    @Autowired
    private UserRepository userRepository; // 또는 Service

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null이거나 빈 값이면 유효성(중복 아닌 것은 그냥 통과)
        if (value == null || value.isBlank())
            return true;

        // userRepository.findByEmail(value) 등으로 중복 검사
        return !userRepository.existsById(value);
    }
}
