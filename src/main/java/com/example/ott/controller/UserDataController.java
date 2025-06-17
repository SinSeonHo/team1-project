package com.example.ott.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.ott.entity.User;
import com.example.ott.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Log4j2
@RestController
@RequiredArgsConstructor
public class UserDataController {

    private final UserRepository userRepository;


    @GetMapping("/users")
    public List<User> getUsers() {
        
        return userRepository.findAll();
    }
    
    
        
}
