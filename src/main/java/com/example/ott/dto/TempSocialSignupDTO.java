package com.example.ott.dto;

import java.util.Map;

import com.example.ott.entity.Socials;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TempSocialSignupDTO {
    private String email; // 소셜에서 가져온 이메일
    private String name; // 소셜에서 가져온 이름 (없을 수 있음)
    private String nickname; // 소셜에서 가져온 닉네임
    private String picture; // 프로필 이미지 URL
    private Socials social; // GOOGLE, KAKAO, NAVER 등

    public static TempSocialSignupDTO from(Map<String, Object> attrs) {
        return TempSocialSignupDTO.builder()
                .email((String) attrs.get("email"))
                .name((String) attrs.getOrDefault("name", null))
                .nickname((String) attrs.getOrDefault("nickname", null))
                .picture((String) attrs.getOrDefault("picture", null))
                .social((Socials) attrs.get("social"))
                .build();
    }
}