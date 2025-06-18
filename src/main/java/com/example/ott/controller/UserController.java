package com.example.ott.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.ott.entity.User;
import com.example.ott.repository.UserRepository;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@RestController
public class UserController {

    private final UserRepository userRepository;

    UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/auth")
    public Authentication getAuth() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication;
    }

    @GetMapping("/users")
    public List<User> getUsers() {

        return userRepository.findAll();
    }

    @GetMapping("/myprofile")
    public String myPage() {
        return "myprofile"; // templates/mypage.html
    }
}
