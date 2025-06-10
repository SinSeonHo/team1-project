package com.example.ott.security;


import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


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
        // 로그인 요청 정보 확인
        log.info("USERNAME TEST : {}", userRequest);

        // 클라이언트 이름, 토큰 추출
        String clientName = userRequest.getClientRegistration().getClientName(); // 소셜 종류(Google, Kakao, Naver, ...)
        log.info("========================OAuth2User가 가지고 있는 값");
        log.info("clientName{}", clientName);
        log.info(userRequest.getAdditionalParameters());
        log.info("========================");
        
        OAuth2User oAuth2User = super.loadUser(userRequest); // 소셜에 rest 요청
        // 소셜 사용자 정보 로드, 키:값 형태로 로깅
        oAuth2User.getAttributes().forEach((key, value) -> {
            log.info("{} : {}", key, value);
        });

        // 사용자 이메일 추출(소셜 계정이 구글일 경우에만)
        // 소셜 계정 확장시 해당 코드 분기 확장 소요 있음
        String email = "";
        if(clientName.equals("Google")) 
            email = oAuth2User.getAttribute("email");

        User user = saveSocialUser(email);

        CustomUserDetails customUserDetails = new CustomUserDetails(user, oAuth2User.getAttributes());

        return customUserDetails;
    }
    
    private User saveSocialUser(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
           User saveUser = User.builder()
            .id(email)
            .email(email)
            .name("test")
            .password(passwordEncoder.encode("1111"))
            .userRole(UserRole.USER)
            .build();
            userRepository.save(saveUser);
            
            return saveUser;
        }

        return user;
    }
    

    
}
