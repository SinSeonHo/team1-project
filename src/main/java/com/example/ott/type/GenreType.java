package com.example.ott.type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum GenreType {
    MMO("MMO"),
    RPG("알피지"),
    FPS("FPS"),
    ADVENTURE("모험"),
    STRATEGY("전략"),
    SIMULATION("시뮬레이션"),
    SPORTS("스포츠"),
    PUZZLE("퍼즐"),
    ACTION("액션"),
    HORROR("호러"),
    COMEDY("코미디"),
    ROMANCE("로맨스"),
    FANTASY("판타지"),
    DRAMA("드라마"),
    THRILLER("스릴러"),
    SLICE_OF_LIFE("일상물"),
    SCHOOL("학원물"),
    SCI_FI("공상과학"),
    ANIMATION_MODELING("애니메이션&모델링"),
    DESIGN_ILLUSTRATION("디자인&일러스트레이션"),
    PHOTO_EDITING("사진편집"),
    UTILITES("유틸"),
    RACING("레이싱"),
    FREE_TO_PLAY("무료플레이"),
    INDIE("인디"),
    CASUAL("캐쥬얼"),
    MASSIVELY_MULTIPLAYER("대규모 멀티플레이어"),
    EARLY_ACCESS("얼리엑세스"),
    DOCUMENTARY("다큐멘터리"),
    // 필요에 따라 더 추가
    UNKNOWN("장르정보없음");

    private final String koreanName;

    GenreType(String koreanName) {
        this.koreanName = koreanName;
    }

    public String getKorean() {
        return koreanName;
    }

    /**
     * 한글 → Enum 객체 변환
     */
    public static GenreType engKorean(String korean) {
        for (GenreType type : values()) {
            if (type.koreanName.equals(korean)) {
                return type;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 장르입니다: " + korean);
    }

    /**
     * 영문 enum name → 한글 변환
     */
    public static String toKorean(String eng) {
        for (GenreType type : values()) {
            if (type.name().equalsIgnoreCase(eng)) {
                return type.getKorean();
            }
        }
        return UNKNOWN.getKorean(); // 일치하는 enum 없을 경우
    }

    /**
     * 쉼표로 구분된 한글 or 영문 → Enum Set 변환
     */
    public static Set<GenreType> enumGenre(String pgenre) {
        if (pgenre == null || pgenre.isBlank())
            return Set.of();

        Set<GenreType> result = new HashSet<>();

        for (String raw : pgenre.split(",")) {
            String trimmed = raw.trim();

            // 대소문자 무시하고 영문 enum 이름 찾기
            try {
                result.add(GenreType.valueOf(trimmed.toUpperCase()));
                continue;
            } catch (IllegalArgumentException ignored) {
            }

            // 한글 이름 찾기
            try {
                result.add(engKorean(trimmed));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return result;
    }
}
