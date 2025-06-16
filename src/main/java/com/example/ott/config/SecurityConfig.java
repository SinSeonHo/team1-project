package com.example.ott.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity(debug = true) // debug 확인용, 배포시 삭제해야함
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // csrf 임시 비활성화
        http
                .csrf(csrf -> csrf.disable());

        // localhost:8080/auth 를 제외한 모든 경로 인증 확인
        http
                .authorizeHttpRequests(authorize -> authorize
                        // .requestMatchers("/auth")
                        .anyRequest().permitAll()
                // .anyRequest().authenticated()
                );

        // 현재 로그인 페이지는 security 기본 제공, 로그인 성공 시 "localhost:8080/"로 이동
        http
                .formLogin(login -> login.defaultSuccessUrl("/"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
