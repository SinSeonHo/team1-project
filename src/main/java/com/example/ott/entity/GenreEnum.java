package com.example.ott.entity;

public enum GenreEnum {
    ACTION("Action", "액션"),
    ADVENTURE("Adventure", "어드벤처"),
    STRATEGY("Strategy", "전략"),
    INDIE("Indie", "인디"),
    RPG("RPG", "롤플레잉"),
    SIMULATION("Simulation", "시뮬레이션"),
    SPORTS("Sports", "스포츠"),
    RACING("Racing", "레이싱"),
    CASUAL("Casual", "캐주얼"),
    MMO("Massively Multiplayer", "MMO"),
    FREE_TO_PLAY("Free to Play", "무료 플레이"),
    EARLY_ACCESS("Early Access", "얼리액세스"),
    // 필요에 따라 더 추가
    UNKNOWN("Unknown", "장르정보없음");

    private final String engName;
    private final String korName;

    GenreEnum(String engName, String korName) {
        this.engName = engName;
        this.korName = korName;
    }

    public static String toKorean(String eng) {
        for (GenreEnum genre : values()) {
            if (genre.engName.equalsIgnoreCase(eng)) {
                return genre.korName;
            }
        }
        return eng; // 매칭 안 되면 원래 영어 그대로 리턴 (또는 "기타" 등)
    }
}