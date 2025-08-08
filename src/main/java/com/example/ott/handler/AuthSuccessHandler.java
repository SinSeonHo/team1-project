package com.example.ott.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.ott.security.CustomUserDetails;

@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REGISTER_SESSION_KEY = "REGISTER_TEMP";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws ServletException, java.io.IOException {

        Object principal = authentication.getPrincipal();

        // 우리 애플리케이션의 Principal만 처리
        if (principal instanceof CustomUserDetails cud) {

            // ✅ 임시 DTO가 있으면(일반 회원가입이든 소셜 회원가입이든) register로
            if (cud.getTemp() != null) {
                request.getSession().setAttribute(REGISTER_SESSION_KEY, cud.getTemp());
                getRedirectStrategy().sendRedirect(request, response, "/user/register");
                return;
            }

            // ✅ 임시 DTO가 없으면: 소셜 기존 회원/일반 로그인 → 홈으로
            getRedirectStrategy().sendRedirect(request, response, "/");
            return;
        }

        // 예상치 못한 Principal 타입이면 홈으로
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}