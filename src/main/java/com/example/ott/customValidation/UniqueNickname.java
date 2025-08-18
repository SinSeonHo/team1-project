package com.example.ott.customValidation;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueNicknameValidator.class)
public @interface UniqueNickname {
    String message() default "이미 등록된 닉네임입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
