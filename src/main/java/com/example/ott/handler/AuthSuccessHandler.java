package com.example.ott.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.entity.Socials;
import com.example.ott.entity.UserRole;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.type.SessionKeys;

@Component
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

     // 필요 시 기존 사용자일 때의 기본 이동 경로
    private static final String DEFAULT_SUCCESS_URL = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            TempSocialSignupDTO temp = userDetails.getTemp();

            // 1) PENDING이면 회원가입 플로우 진입
            if (temp != null && userDetails.getSecurityUserDTO().getUserRole() == UserRole.PENDING ) {
                HttpSession session = request.getSession(true);     // 세션 생성
                session.setAttribute(SessionKeys.TEMP_SOCIAL, temp); // 임시 DTO 저장
                session.setMaxInactiveInterval(15 * 60);             // 15분 등 임시 만료

                getRedirectStrategy().sendRedirect(request, response, "/user/register");
                return;
            }
        }

        // 2) 그 외(정상 로그인/기존 회원): 저장된 요청(SavedRequest) 또는 기본 URL로
        setDefaultTargetUrl(DEFAULT_SUCCESS_URL);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    }
