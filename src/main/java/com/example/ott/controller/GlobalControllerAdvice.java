package com.example.ott.controller;

import java.util.NoSuchElementException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.ott.entity.User;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    // header에 이미지 정보 상시 추가
    @ModelAttribute
    public void currentUser(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.getUserById(userDetails.getUsername());
            if (user == null)
                return;
            String id = user.getId();
            if (user.getImage() != null) {
                model.addAttribute("profileImageUrl", user.getImage().getThumbnailPath());

            }
            model.addAttribute("userId", id);
        }
    }

    // 커스텀 에러
    @ExceptionHandler(NoSuchElementException.class)
    public String noSuchUserException() {

        return "error/noSuchUser";
    }
}
