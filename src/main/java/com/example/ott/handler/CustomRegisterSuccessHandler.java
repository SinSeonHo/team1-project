package com.example.ott.handler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.ott.security.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomRegisterSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        System.out.println("핸들러 진입");

        // 데이터 생성일과 데이터 변경일이 동일할 경우 계정 최초 로그인으로 인식
        boolean isFirstLogin = userDetails.getCreatedDate().equals(userDetails.getUpdatedDate());

        if (isFirstLogin) {
            response.sendRedirect("/user/modifyUserProfile" + "?id=" + userDetails.getUsername()); // 최초 로그인일시 프로필 설정으로
                                                                                                   // 이동
        } else {
            response.sendRedirect("/");

        }
    }
}
