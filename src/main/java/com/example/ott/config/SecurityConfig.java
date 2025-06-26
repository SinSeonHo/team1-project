package com.example.ott.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.ott.entity.User;
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

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // 정적 리소스
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                                                "/social/**")
                                                .permitAll()

                                                // 에러페이지/홈/회원가입/인증 관련
                                                .requestMatchers("/", "/user/register", "/error/**", "/auth",
                                                                "/user/upgrade")
                                                .permitAll()

                                                // 영화 관련
                                                .requestMatchers("/api/movies/import").hasRole("ADMIN")
                                                .requestMatchers("/api/movies/**").permitAll()

                                                // 게임 관련
                                                .requestMatchers("/api/games/import").hasRole("ADMIN")
                                                .requestMatchers("/api/games/**").permitAll()

                                                .requestMatchers(HttpMethod.GET, "/replies/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/replies/**").authenticated()
                                                .requestMatchers(HttpMethod.PUT, "/replies/**").authenticated()
                                                .requestMatchers(HttpMethod.DELETE, "/replies/**").authenticated()

                                                // 유저 관련(로그인 필요)
                                                .requestMatchers("/user/modifyUserProfile", "/user/uploadProfile",
                                                                "/user/userProfile", "/user/delete")
                                                .authenticated()

                                                // 기타 모든 경로는 인증 필요
                                                .anyRequest().authenticated());
                http
                                // 일반 로그인
                                .formLogin(login -> login
                                                .loginPage("/user/login")
                                                .defaultSuccessUrl("/", true)
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
}
