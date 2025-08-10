package com.example.ott.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;

import com.example.ott.handler.AuthSuccessHandler;

import com.example.ott.security.CustomOAuth2DetailsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity(debug = false)
@Configuration
public class SecurityConfig {

        private final CustomOAuth2DetailsService customOAuth2DetailsService;

        private final AuthenticationConfiguration authenticationConfiguration;

        @Bean
        public AuthenticationManager authenticationManager() throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                // 정적 리소스
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**",
                                                                "/social/**", "/auth/**")
                                                .permitAll()

                                                // 에러페이지/홈/회원가입/인증 관련
                                                .requestMatchers("/", "/error/**",
                                                                "/user/upgrade", "/user/userConsent")
                                                .permitAll()

                                                .requestMatchers("/user/register").permitAll()

                                                // 영화 관련
                                                // .requestMatchers("/api/movies/import").hasRole("ADMIN")
                                                .requestMatchers("/api/movies/**").permitAll()
                                                .requestMatchers("/movies/**").permitAll()

                                                // 게임 관련
                                                // .requestMatchers("/api/games/import").hasRole("ADMIN")
                                                .requestMatchers("/api/games/**").permitAll()
                                                .requestMatchers("/games/**").permitAll()

                                                // 유저 관련
                                                .requestMatchers(HttpMethod.GET, "/replies/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/replies/**")
                                                .hasAnyRole("USER", "MANAGER", "ADMIN")
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
                                                .successHandler(new AuthSuccessHandler())
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
