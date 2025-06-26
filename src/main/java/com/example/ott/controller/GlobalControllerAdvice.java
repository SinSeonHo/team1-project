package com.example.ott.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {
    @ModelAttribute("userDetails")
    public UserDetails currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;
    }
}
