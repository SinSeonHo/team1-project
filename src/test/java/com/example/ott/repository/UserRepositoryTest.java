package com.example.ott.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;

@SpringBootTest
public class UserRepositoryTest {
 
    @Autowired
    private UserRepository userRepository;
    

    @Test
    public void userInsertTest() {
        User user1 = User.builder()
        .id("user1")
        .name("신짱구")
        .email("user1@gmail.com")
        .password(passwordEncoder().encode("1111"))
        .userRole(UserRole.USER)
        .mileage(10L)
        .build();

        userRepository.save(user1);
        User user2 = User.builder()
        .id("user2")
        .name("신형만")
        .email("user2@gmail.com")
        .password(passwordEncoder().encode("1111"))
        .userRole(UserRole.MANAGER)
        .mileage(10L)
        .build();

        userRepository.save(user2);
        User user3 = User.builder()
        .id("user3")
        .name("봉미선")
        .email("user3@gmail.com")
        .password(passwordEncoder().encode("1111"))
        .userRole(UserRole.ADMIN)
        .mileage(10L)
        .build();

        userRepository.save(user3);
    }

    @Bean
    private PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
