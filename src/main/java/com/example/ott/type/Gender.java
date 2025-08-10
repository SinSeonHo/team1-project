package com.example.ott.type;

public enum Gender {
    MAN, WOMAN, UNKNOWN;

     public static Gender fromString(String value) {
        if (value == null) return UNKNOWN;
        try {
            return Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
