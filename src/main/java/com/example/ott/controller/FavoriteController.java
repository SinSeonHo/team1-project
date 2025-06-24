package com.example.ott.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ott.entity.User;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.service.FavoriteService;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/favorite")
@RestController
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping("/toggle")
    public void toggleFavorite(@AuthenticationPrincipal UserDetails userDetails, String contentsId) {

        User user = userService.getUser(userDetails.getUsername());
        favoriteService.toggleFavorite(user, contentsId);

    }

}
