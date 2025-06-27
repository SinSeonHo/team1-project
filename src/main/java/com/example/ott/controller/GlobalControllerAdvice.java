package com.example.ott.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.ott.entity.User;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    // header에서 필요한 내용
    @ModelAttribute
    public void currentUser(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.getUser(userDetails.getUsername());
            String id = user.getId();
            model.addAttribute("grade", user.getGrade());
            if (user.getImage() != null) {
                model.addAttribute("profileImageUrl", user.getImage().getThumbnailPath());

            }
            model.addAttribute("userId", id);
        }
    }
}
