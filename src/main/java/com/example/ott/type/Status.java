package com.example.ott.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Status {
    @JsonEnumDefaultValue
    RECEIVED, // 접수됨
    WARNING, // 경고
    DELETED, // 삭제
    NO_ACTION; // 무혐의

    /** null/빈값/모르는 값 -> RECEIVED 로 보정 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Status fromJson(String value) {
        if (value == null || value.isBlank())
            return RECEIVED;
        try {
            return Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return RECEIVED;
        }
    }

    /** 코드 내에서 안전하게 기본값 보정이 필요할 때 */
    public static Status orDefault(Status s) {
        return (s == null) ? RECEIVED : s;
    }
}