package com.example.ott.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;

import com.example.ott.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class CustomOAuth2DetailsService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 소셜 로그인 정보 처리 메서드
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // userRequest : Resource server에 요청을 하기 위해 access Token과 client id, Secret,
        // scope, RedirectionURi을 객체로 감싼 ClientRegistration 정보를 넣어둠

        // 클라이언트 이름, 토큰 추출
        String clientName = userRequest.getClientRegistration().getClientName(); // 소셜 종류(Google, Kakao, Naver, ...)

        OAuth2User oAuth2User = super.loadUser(userRequest); // Resource server에 Resource 요청

        // oAuth2User : Resource Server에서 제공해준 Resources (Map)
        // TODO : 요청했는데 받지 못할 경우(1. Access Token 만료, scope 미지정 or 부족, Resource Server의
        // 오류, 사용자가 소셜 서비스에서 정보 제공을 거부, 잘못된 엔드포인트나 설정 오류, 서비스 정책 변경)
        // 이 부분을 try-catch로 감싸서 customErrorHandler를 만들어서 별도의 안내 페이지로 리디렉트하는 식으로 처리해야함.

        String email = "";
        String name = "";
        Socials socials = Socials.NONE;

        // 일치하는 소셜에 따라 email, name 정보 추출, User socials 속성 지정
        switch (clientName) {
            case "Google":
                email = oAuth2User.getAttribute("email");
                name = oAuth2User.getAttribute("name");
                socials = Socials.GOOGLE;

                break;
            case "kakao":
                // attributes Map 얻기
                Map<String, Object> attributes = oAuth2User.getAttributes();

                // nickname 꺼내기
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                // name = (String) properties.get("nickname");
                name = ""; // kakao 성명 받아오는 권한이 없음

                // email 꺼내기
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) kakaoAccount.get("email");
                socials = Socials.KAKAO;
                break;

            case "Naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                email = (String) response.get("email");
                name = (String) response.get("name");
                socials = Socials.NAVER;
                log.info("naver 정보들 {} {} {}", response, email, name);

                break;

            default:
                socials = Socials.NONE;
                break;
        }

        // 필요한 값만 CustomUserDetails의 attributes에 추가
        Map<String, Object> customAttributes = new HashMap<String, Object>();
        customAttributes.put("name", name);
        customAttributes.put("email", email);

        User user = saveSocialUser(email, name, socials);

        CustomUserDetails customUserDetails = new CustomUserDetails(user, customAttributes);

        return customUserDetails;
    }

    // 이메일이 DB에 존재할 경우 반환, 존재하지 않을 경우 DB에 추가
    private User saveSocialUser(String email, String name, Socials socials) {
        // 1. 현재 로그인된 유저 체크
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            String currentUserId = ((CustomUserDetails) auth.getPrincipal()).getUsername();
            User currentUser = userRepository.findById(currentUserId).get();
            if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {

                if (userRepository.existsByEmail(email)) {
                    throw new OAuth2AuthenticationException("이미 등록된 이메일입니다."); // 기존 로그인 상황에서 존재하는 소셜 계정으로 인증을 시도 할 경우
                } else {

                    // 이메일 없는 기존 회원 → 소셜 이메일 추가
                    currentUser.setEmail(email);
                    currentUser.setSocials(socials);
                    userRepository.save(currentUser);
                    return currentUser;
                }
            }
        }

        // 2. 기존 방식
        User user = userRepository.findByEmail(email);
        if (user == null) {
            // 신규 회원
            User saveUser = User.builder()
                    .id(email)
                    .email(email)
                    .name(name)
                    .nickname(makeUniqueNickname(userRepository))
                    .password(passwordEncoder.encode("1111"))
                    .userRole(UserRole.USER)
                    .socials(socials)
                    .build();
            userRepository.save(saveUser);
            return userRepository.findByEmail(email);
        }
        // throw new RuntimeException("이미 가입된 이메일입니다.");
        return user;
    }

    // 임시 닉네임 생성
    public String makeUniqueNickname(UserRepository userRepository) {
        String candidate;
        do {
            candidate = "user" + (int) (Math.random() * 100000);
        } while (userRepository.existsByNickname(candidate));
        return candidate;
    }

}
