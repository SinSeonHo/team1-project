package com.example.ott.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.ott.type.SessionKeys;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class LinkSocialController {

    // 버튼에서 /user/link/google, /user/link/kakao 등으로 요청
    @PostMapping("/link/{provider}")
    public String linkSocial(@PathVariable String provider,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            HttpSession session) {
        if (principal == null)
            return "redirect:/user/login";
        // GUEST만 연동 허용 (원하면 제거 가능)
        // isGUEST 체크는 서비스/리포지토리로 principal.getUsername() → UserRole 조회해서 검사하세요.
        session.setAttribute(SessionKeys.LINK_SOCIAL_USER_ID, principal.getUsername());
        // 표준 OAuth2 엔드포인트로 리다이렉트
        return "redirect:/oauth2/authorization/" + provider.toLowerCase();
    }

    private final ClientRegistrationRepository clientRegistrations;

    @PostMapping("/user/link/{provider}")
    public String link(@PathVariable String provider,
            @AuthenticationPrincipal UserDetails principal,
            HttpSession session) {
        String id = provider.toLowerCase(); // google/kakao/naver
        ClientRegistration reg = ((InMemoryClientRegistrationRepository) clientRegistrations).findByRegistrationId(id);

        if (reg == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "알 수 없는 소셜 제공자: " + provider);
        }

        session.setAttribute(SessionKeys.LINK_SOCIAL_USER_ID, principal.getUsername());
        return "redirect:/oauth2/authorization/" + reg.getRegistrationId();
    }
}