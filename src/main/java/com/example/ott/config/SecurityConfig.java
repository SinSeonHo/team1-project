package com.example.ott.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.ott.handler.CustomRegisterSuccessHandler;
import com.example.ott.security.CustomOAuth2DetailsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity(debug = false) // debug 확인용, 배포시 삭제해야함
@Configuration
public class SecurityConfig {

        private final CustomOAuth2DetailsService customOAuth2DetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // csrf 임시 비활성화
                http
                                .csrf(csrf -> csrf.disable());

                // localhost:8080/auth 를 제외한 모든 경로 인증 확인
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                                                "/user/admin")
                                                .permitAll()
                                                .anyRequest().permitAll());

                // 현재 로그인 페이지는 security 기본 제공, 로그인 성공 시 "localhost:8080/"로 이동
                http
                                // 일반 로그인
                                .formLogin(login -> login
                                                .loginPage("/user/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/user/login?error=true")
                                                .permitAll())

                                // admin 로그인
                                .formLogin(login -> login
                                                .loginPage("/user/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/user/login?error=true")
                                                .permitAll())

                                // 소셜 로그인
                                .oauth2Login(login -> login
                                                .loginPage("/user/login")
                                                .successHandler(new CustomRegisterSuccessHandler())
                                                .failureHandler((request, response, exception) -> {
                                                        response.sendRedirect("/error/emailAlreadyExists");
                                                })
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2DetailsService)));

                http
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/"));

                return http.build();
        }

        // CORS 에러
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        // 회원가입 시 자동 로그인을 위한 빈 등록
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}
