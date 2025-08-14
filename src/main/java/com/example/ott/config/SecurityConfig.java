package com.example.ott.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;

import com.example.ott.handler.AuthSuccessHandler;

import com.example.ott.security.CustomOAuth2DetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomOAuth2DetailsService customOAuth2DetailsService;

        private final AuthenticationConfiguration authenticationConfiguration;

        private final AuthSuccessHandler authSuccessHandler;

        @Bean
        public AuthenticationManager authenticationManager() throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // http.csrf().disable();
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // 정적 리소스 접근 허용
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",

                                                                "/social/**", "/auth/**")

                                                .permitAll()

                                                // 신고 페이지 (ADMIN만 허용)
                                                .requestMatchers("/report/**").permitAll()
                                                // .requestMatchers("/report", "/WEB-INF/**").hasRole("ADMIN")

                                                // 에러페이지/홈/회원가입/인증 관련

                                                .requestMatchers("/", "/error/**",
                                                                "/user/upgrade", "/user/userConsent", "/user/register")

                                                .permitAll()

                                                // 어드민 페이지 관련
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // 영화 관련
                                                // .requestMatchers("/api/movies/import").hasRole("ADMIN")
                                                .requestMatchers("/api/movies/**").permitAll()
                                                .requestMatchers("/movies/**").permitAll()

                                                // 게임 관련
                                                // .requestMatchers("/api/games/import").hasRole("ADMIN")
                                                .requestMatchers("/api/games/**").permitAll()
                                                .requestMatchers("/games/**").permitAll()

                                                // 컨텐츠 관련
                                                .requestMatchers("/contents/**").permitAll()
                                                // 댓글 관련
                                                .requestMatchers(HttpMethod.GET, "/replies/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/replies/**")
                                                .hasAnyRole("USER", "MANAGER", "ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/replies/**").authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/replies/**").authenticated()

                                                // 신고 관련 이게 찐임

                                                // .requestMatchers(HttpMethod.GET, "/report/list").hasRole("ADMIN")
                                                // .requestMatchers(HttpMethod.POST, "/report")
                                                // .hasAnyRole("USER", "MANAGER", "ADMIN")
                                                // .requestMatchers(HttpMethod.PATCH, "/report/**").hasRole("ADMIN")
                                                // .requestMatchers(HttpMethod.DELETE, "/report/**").hasRole("ADMIN")

                                                // 유저 관련(로그인 필요)
                                                .requestMatchers("/user/modifyUserProfile", "/user/uploadProfile",
                                                                "/user/userProfile", "/user/delete")
                                                .authenticated()

                                                // 기타 모든 경로는 인증 필요
                                                .anyRequest().authenticated());

                // [추가] iframe 허용 설정 (report.jsp iframe 삽입을 위해 sameOrigin 허용)
                http
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions
                                                                .sameOrigin()));

                // 일반 로그인
                http.formLogin(login -> login
                                .loginPage("/user/login")
                                .loginProcessingUrl("/user/login")
                                .defaultSuccessUrl("/", true)
                                .failureHandler((request, response, ex) -> {
                                        String message;
                                        if (ex instanceof DisabledException || ex instanceof LockedException) {
                                                message = "관리자에 의해 정지된 계정입니다.";
                                        } else if (ex instanceof BadCredentialsException
                                                        || ex instanceof UsernameNotFoundException) {
                                                message = "아이디 또는 비밀번호가 올바르지 않습니다.";
                                        } else {
                                                message = "로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.";
                                        }

                                        // 표준 키로 예외를 세션에 저장 (뷰에서 꺼내 쓰기 쉬움)
                                        request.getSession().setAttribute(
                                                        WebAttributes.AUTHENTICATION_EXCEPTION,
                                                        new AuthenticationServiceException(message, ex));

                                        // 컨텍스트 경로 고려
                                        String ctx = request.getContextPath();
                                        response.sendRedirect(ctx + "/user/login?error");
                                })
                                .permitAll());

                http.oauth2Login(o -> o
                                .loginPage("/user/login")
                                .userInfoEndpoint(u -> u.userService(customOAuth2DetailsService))
                                .successHandler(authSuccessHandler) // ✔ new 제거, 주입한 빈 사용
                                .failureHandler((request, response, ex) -> {
                                        String ctx = request.getContextPath();
                                        if (ex instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oae) {
                                                String code = oae.getError() != null ? oae.getError().getErrorCode()
                                                                : null;
                                                String desc = oae.getError() != null ? oae.getError().getDescription()
                                                                : null;
                                                if ("email_already_exists".equals(code)) {
                                                        response.sendRedirect(ctx + "/error/emailAlreadyExists");
                                                        return;
                                                }
                                                String msg = (desc != null && !desc.isBlank())
                                                                ? desc
                                                                : "소셜 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요.";
                                                request.getSession().setAttribute(
                                                                org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION,
                                                                new org.springframework.security.authentication.AuthenticationServiceException(
                                                                                msg, oae));
                                                response.sendRedirect(ctx + "/user/login?error");
                                                return;
                                        }
                                        request.getSession().setAttribute(
                                                        org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION,
                                                        new org.springframework.security.authentication.AuthenticationServiceException(
                                                                        "소셜 로그인에 실패했습니다.", ex));
                                        response.sendRedirect(ctx + "/user/login?error");
                                }));
                // 로그아웃 설정
                http
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/"));

                return http.build();
        }
}
