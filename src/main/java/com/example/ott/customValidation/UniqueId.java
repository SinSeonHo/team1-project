package com.example.ott.customValidation;

import java.lang.annotation.*;

import jakarta.validation.Payload;

@Documented
@jakarta.validation.Constraint(validatedBy = UniqueIdValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueId {
    String message() default "이미 등록된 이메일입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
