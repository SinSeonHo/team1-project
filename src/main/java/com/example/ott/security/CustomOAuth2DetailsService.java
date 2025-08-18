package com.example.ott.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.repository.UserRepository;
import com.example.ott.type.Gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class CustomOAuth2DetailsService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // (미사용이면 제거해도 됨)

    private static final String ERR_EMAIL_MISSING = "email_missing";
    private static final String ERR_PROVIDER_UNSUPPORTED = "provider_unsupported";
    private static final String ERR_EMAIL_ALREADY_EXISTS = "email_already_exists";
    private static final String ERR_ACCESS_DENIED = "access_denied";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String name = "";
        String picture = "";
        String email = "";
        String nickname = "";
        Gender gender = null;
        Socials social = Socials.NONE;
        Map<String, Object> customAttributes = new HashMap<>();

        switch (registrationId) {
            case "google" -> {
                name = Objects.toString(oAuth2User.getAttribute("name"), "");
                picture = Objects.toString(oAuth2User.getAttribute("picture"), "");
                email = Objects.toString(oAuth2User.getAttribute("email"), "");
                social = Socials.GOOGLE;
                customAttributes.put("name", name);
                customAttributes.put("picture", picture);
                customAttributes.put("email", email);
            }
            case "kakao" -> {
                Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttribute("properties");
                Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
                nickname = properties != null ? Objects.toString(properties.get("nickname"), "") : "";
                picture = properties != null ? Objects.toString(properties.get("profile_image"), "") : "";
                email = kakaoAccount != null ? Objects.toString(kakaoAccount.get("email"), "") : "";
                social = Socials.KAKAO;
                customAttributes.put("nickname", nickname);
                customAttributes.put("picture", picture);
                customAttributes.put("email", email);
            }
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                name = response != null ? Objects.toString(response.get("name"), "") : "";
                nickname = response != null ? Objects.toString(response.get("nickname"), "") : "";
                String genderStr = response != null ? Objects.toString(response.get("gender"), "") : "";
                gender = Gender.fromString(genderStr);
                email = response != null ? Objects.toString(response.get("email"), "") : "";
                picture = response != null ? Objects.toString(response.get("profile_image"), "") : "";
                social = Socials.NAVER;
                customAttributes.put("name", name);
                customAttributes.put("nickname", nickname);
                customAttributes.put("gender", gender);
                customAttributes.put("email", email);
                customAttributes.put("picture", picture);
            }
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error(ERR_PROVIDER_UNSUPPORTED, "지원하지 않는 소셜 로그인입니다: " + registrationId, null));
        }

        customAttributes.put("social", social);
        log.info("email 정보 : {}", email);
        customAttributes.put("resolved_email", email);

        // 이메일 없음 → 실패
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(ERR_EMAIL_MISSING, "이메일 정보를 제공하지 않는 소셜 계정입니다. 이메일 제공에 동의해주세요.", null));
        }

        User user = userRepository.findByEmail(email); // null 반환 설계 기준

        // 이미 가입된 사용자 로그인 분기
        if (user != null) {
            if (user.getUserRole() == UserRole.BAN) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(ERR_ACCESS_DENIED, "관리자에 의해 정지된 계정입니다.", null));
            }
            if (user.getSocial() == Socials.NONE || user.getSocial() != social) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(ERR_EMAIL_ALREADY_EXISTS, "이미 가입된 이메일입니다.", null));
            }

            // ✅ 원본 + customAttributes 병합
            Map<String, Object> merged = new HashMap<>(oAuth2User.getAttributes());
            merged.putAll(customAttributes); // resolved_email 포함
            return new CustomUserDetails(user, merged);
        }

        // 신규(PENDING) 분기
        TempSocialSignupDTO tempDTO = TempSocialSignupDTO.from(customAttributes);
        // ✅ 원본 + customAttributes 병합
        Map<String, Object> merged = new HashMap<>(oAuth2User.getAttributes());
        merged.putAll(customAttributes);
        return new CustomUserDetails(tempDTO, merged);
    }
}
