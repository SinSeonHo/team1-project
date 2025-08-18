package com.example.ott.handler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.repository.UserRepository;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.type.SessionKeys;
import com.example.ott.type.UserRole;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String DEFAULT_SUCCESS_URL = "/";

    // ✅ 연동 처리 및 재인증을 위해 주입
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        // ✅ 1) 연동 모드 먼저 확인 (맨 위!)
        String linkingUserId = (session != null)
                ? (String) session.getAttribute(SessionKeys.LINK_SOCIAL_USER_ID)
                : null;

        if (linkingUserId != null && authentication instanceof OAuth2AuthenticationToken token) {
            String provider = token.getAuthorizedClientRegistrationId(); // google/kakao/naver

            Map<String, Object> attrs = ((OAuth2User) authentication.getPrincipal()).getAttributes();
            String email = (String) attrs.get("resolved_email");
            if (email == null || email.isBlank()) {
                email = resolveEmail(provider, attrs); // fallback
            }
            if (email == null || email.isBlank()) {
                // 연동 실패 → 프로필 화면으로 메시지 전달
                clearLinkFlag(request);
                String target = request.getContextPath()
                        + "/user/userProfile?id=" + URLEncoder.encode(linkingUserId, StandardCharsets.UTF_8)
                        + "&linkError=" + URLEncoder.encode("소셜에서 이메일을 제공하지 않았습니다. 제공 동의 후 다시 시도해 주세요.",
                                StandardCharsets.UTF_8);
                response.sendRedirect(target);
                return;
            }

            // 1) 다른 유저가 이미 사용 중이면 중단
            User existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail != null && !existingByEmail.getId().equals(linkingUserId)) {
                clearLinkFlag(request);
                response.sendRedirect(request.getContextPath() + "/error/emailAlreadyExists");
                return;
            }

            // 2) 대상 유저 업데이트 (이메일 추가 + ROLE_USER 승급 + provider 저장)
            User linkingUser = userRepository.findById(linkingUserId)
                    .orElseThrow(() -> new IllegalStateException("연동 대상 사용자가 존재하지 않습니다."));

            linkingUser.setEmail(email);
            linkingUser.setSocial(Socials.valueOf(provider.toUpperCase()));
            if (linkingUser.getUserRole() == UserRole.GUEST) {
                linkingUser.setUserRole(UserRole.USER);
            }
            userRepository.save(linkingUser); // Spring Data JPA 기본적으로 @Transactional 처리됨

            // 3) 최신 UserDetails로 재인증 (UI 즉시 반영)
            var userDetails = userDetailsService.loadUserByUsername(linkingUserId);
            var newAuth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(newAuth);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // 4) 플래그 제거 + 프로필로 성공 안내
            clearLinkFlag(request);
            response.sendRedirect(request.getContextPath()
                    + "/user/userProfile?id=" + URLEncoder.encode(linkingUserId, StandardCharsets.UTF_8)
                    + "&linkSuccess=" + URLEncoder.encode("이메일이 연동되었고 권한이 USER로 승급되었습니다.", StandardCharsets.UTF_8));
            return;
        }

        // ⬇️ 2) 그다음에 PENDING 회원가입 분기 (기존 로직)
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            TempSocialSignupDTO temp = cud.getTemp();
            if (temp != null && cud.getSecurityUserDTO().getUserRole() == UserRole.PENDING) {
                HttpSession newSession = request.getSession(true);
                newSession.setAttribute(SessionKeys.TEMP_SOCIAL, temp);
                newSession.setMaxInactiveInterval(15 * 60);
                getRedirectStrategy().sendRedirect(request, response, "/user/register");
                return;
            }
        }

        // 3) 일반 성공
        setDefaultTargetUrl(DEFAULT_SUCCESS_URL);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private void clearLinkFlag(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s != null)
            s.removeAttribute(SessionKeys.LINK_SOCIAL_USER_ID);
    }

    // 프로바이더별 이메일 추출
    // 프로바이더별 이메일 추출 (attrs = principal.getAttributes() 그대로 전달)
    private String resolveEmail(String registrationId, Map<String, Object> attrs) {
        if (attrs == null)
            return null;

        // 0) 혹시 이전 단계에서 정규화 키를 넣었다면 그걸 최우선 사용
        String normalized = asString(attrs.get("resolved_email"));
        if (!isBlank(normalized))
            return normalized.trim();

        String id = registrationId == null ? "" : registrationId.toLowerCase();
        String email = null;

        switch (id) {
            case "google": {
                // 구글은 루트에 "email"
                email = asString(attrs.get("email"));
                break;
            }
            case "kakao": {
                // 카카오는 "kakao_account" 맵 안에 "email"
                Map<String, Object> acct = castMap(attrs.get("kakao_account"));
                if (acct != null) {
                    String e = asString(acct.get("email"));
                    if (!isBlank(e)) {
                        email = e; // 값이 있으면 플래그와 무관하게 바로 사용
                    } else {

                    }
                }
                break;
            }
            case "naver": {
                // 네이버는 루트에 "response" 맵, 그 안에 "email"
                Map<String, Object> res = castMap(attrs.get("response"));
                if (res != null) {
                    email = asString(res.get("email"));
                }
                break;
            }
            default: {
                // 혹시 모를 커스텀/기타의 경우 공통 키 시도
                email = asString(attrs.get("email"));
            }
        }

        return isBlank(email) ? null : email.trim();
    }

    /* === 작은 유틸 === */
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }

    private static String asString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static Boolean asBoolean(Object o) {
        if (o instanceof Boolean b)
            return b;
        if (o instanceof String s)
            return Boolean.parseBoolean(s);
        return null;
    }

}
