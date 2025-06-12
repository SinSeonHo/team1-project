package com.example.ott.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.example.ott.security.CustomUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CustomOAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

                boolean isFirstLogin = userDetails.getName().equals("default");
            

                if (isFirstLogin) {
                    response.sendRedirect("/user/updateProfile"); // 최초 로그인일시 프로필 설정으로 이동
                }
                response.sendRedirect("/");
    }
}
