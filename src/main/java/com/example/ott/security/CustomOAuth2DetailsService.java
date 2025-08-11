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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;

import com.example.ott.repository.UserRepository;
import com.example.ott.type.Gender;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2DetailsService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 소셜 로그인 정보 처리 메서드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // google / kakao / naver
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        // 소셜 로그인에 데이터 요청
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인으로 받을 정보들
        String name = "";
        String picture = "";
        String email = "";
        String nickname = "";
        Gender gender = null;
        Socials social = Socials.NONE;
        Map<String, Object> customAttributes = new HashMap<String, Object>();

        // 소셜 종류마다 다른 정보를 받아옴
        switch (registrationId) {
            case "google":
                name = Objects.toString(oAuth2User.getAttribute("name"), "");
                picture = Objects.toString(oAuth2User.getAttribute("picture"), "");
                email = Objects.toString(oAuth2User.getAttribute("email"), "");
                social = Socials.GOOGLE;

                customAttributes.put("name", name);
                customAttributes.put("picture", picture);
                customAttributes.put("email", email);

                break;

            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) oAuth2User.getAttribute("properties");
                Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");

                nickname = (String) properties.get("nickname");
                picture = (String) properties.get("profile_image");
                email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

                customAttributes.put("nickname", nickname);
                customAttributes.put("picture", picture);
                customAttributes.put("email", email);
                social = Socials.KAKAO;
                break;

            case "naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                name = Objects.toString(response.get("name"), "");
                nickname = Objects.toString(response.get("nickname"), "");
                String genderStr = Objects.toString(response.get("gender"), "");
                gender = Gender.fromString(genderStr);

                email = Objects.toString(response.get("email"), "");
                picture = Objects.toString(response.get("profile_image"), "");
                customAttributes.put("name", name);
                customAttributes.put("nickname", nickname);
                customAttributes.put("gender", gender);
                customAttributes.put("email", email);
                customAttributes.put("picture", picture);
                social = Socials.NAVER;
                break;

            default:
                throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }

        customAttributes.put("social", social);

        // 이메일을 받아오지 못했을 경우 처리
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("이메일 정보를 제공하지 않는 소셜 계정입니다. 이메일 제공에 동의해주세요.");
        }

        User user = userRepository.findByEmail(email);

        // 1) 이미 가입한 user가 있을 경우: 그냥 로그인
        if (user != null) {
            // 원본 provider attributes를 쓰고 싶다면 아래 줄에서 customAttributes 대신
            // oAuth2User.getAttributes() 전달
            return new CustomUserDetails(user, oAuth2User.getAttributes());
        }

        // 2) 계정 없음: 임시 DTO 싣고 소셜 회원가입 분기
        TempSocialSignupDTO tempDTO = TempSocialSignupDTO.from(customAttributes);
        return new CustomUserDetails(tempDTO, oAuth2User.getAttributes());
    }

}

// 이메일이 DB에 존재할 경우 반환, 존재하지 않을 경우 DB에 추가
// private User saveSocialUser(String email, String name, Socials social) {

// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
// // 기존 로그인 사용자인지 확인
// if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof
// CustomUserDetails) {
// String currentUserId = ((CustomUserDetails)
// auth.getPrincipal()).getUsername();
// User currentUser = userRepository.findById(currentUserId).get();
// if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {

// if (userRepository.existsByEmail(email)) {
// throw new OAuth2AuthenticationException("이미 등록된 이메일입니다.");
// } else {

// // 이메일 없는 기존 회원 → 소셜 이메일 추가
// currentUser.setEmail(email);
// currentUser.setSocial(social);
// userRepository.save(currentUser);
// return currentUser;
// }
// }
// }
// // 신규 회원
// User user = userRepository.findByEmail(email);
// if (user == null) {
// User saveUser = User.builder()
// .id(email)
// .email(email)
// .name(name)
// .nickname(makeUniqueNickname(userRepository))
// .password(passwordEncoder.encode("1111"))
// .userRole(UserRole.USER)
// .social(social)
// .build();
// userRepository.save(saveUser);
// return userRepository.findByEmail(email);
// }
// return user;
// }

// private User updateSocialUserIfExists(String email, String name, Socials
// social) {
// // 이미 가입된 회원 찾기
// User user = userRepository.findByEmail(email);
// if (user == null) {
// throw new OAuth2AuthenticationException("등록되지 않은 회원입니다.");
// }

// // 소셜 정보 업데이트
// if (user.getSocial() == null || user.getSocial() == Socials.NONE) {
// user.setSocial(social);
// }

// // 이름이 없으면 소셜에서 받은 이름 저장
// if (user.getName() == null || user.getName().isEmpty()) {
// user.setName(name);
// }

// // 이메일이 없을 경우(혹시 모를 예외 상황)
// if (user.getEmail() == null || user.getEmail().isEmpty()) {
// user.setEmail(email);
// }

// userRepository.save(user);
// return user;
// }
