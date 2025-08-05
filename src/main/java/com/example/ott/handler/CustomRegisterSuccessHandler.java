package com.example.ott.handler;

import java.io.IOException;

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

        // 최초 로그인 체크
        boolean isFirstLogin = userDetails.getCreatedDate().equals(userDetails.getUpdatedDate());

        if (isFirstLogin) {
            response.sendRedirect("/user/modifyUserProfile" + "?id=" + userDetails.getUsername());
        } else {
            response.sendRedirect("/");

        }
    }
}
