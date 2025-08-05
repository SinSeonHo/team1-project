package com.example.ott.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

@Service
@RequiredArgsConstructor
public class CustomOAuth2DetailsService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 소셜 로그인 정보 처리 메서드
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 클라이언트 이름, 토큰 추출
        String clientName = userRequest.getClientRegistration().getClientName();

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = "";
        String name = "";
        Socials socials = Socials.NONE;

        switch (clientName) {
            case "Google":
                email = oAuth2User.getAttribute("email");
                name = oAuth2User.getAttribute("name");
                socials = Socials.GOOGLE;

                break;
            case "kakao":

                Map<String, Object> attributes = oAuth2User.getAttributes();

                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                name = "";

                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                email = (String) kakaoAccount.get("email");
                socials = Socials.KAKAO;
                break;

            case "Naver":
                Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
                email = (String) response.get("email");
                name = (String) response.get("name");
                socials = Socials.NAVER;

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // 기존 로그인 사용자인지 확인
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            String currentUserId = ((CustomUserDetails) auth.getPrincipal()).getUsername();
            User currentUser = userRepository.findById(currentUserId).get();
            if (currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {

                if (userRepository.existsByEmail(email)) {
                    throw new OAuth2AuthenticationException("이미 등록된 이메일입니다.");
                } else {

                    // 이메일 없는 기존 회원 → 소셜 이메일 추가
                    currentUser.setEmail(email);
                    currentUser.setSocials(socials);
                    userRepository.save(currentUser);
                    return currentUser;
                }
            }
        }
        // 신규 회원
        User user = userRepository.findByEmail(email);
        if (user == null) {
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
