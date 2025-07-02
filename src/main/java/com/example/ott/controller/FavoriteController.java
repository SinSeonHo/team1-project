package com.example.ott.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.entity.User;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.service.FavoriteService;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RequestMapping("/favorite")
@RestController
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final UserService userService;

    @GetMapping("/toggle")
    public void toggleFavorite(@AuthenticationPrincipal UserDetails userDetails, String contentsId) {
        System.out.println("도저언");
        User user = userService.getUser(userDetails.getUsername());
        favoriteService.follow(user, contentsId);

    }

    // @ResponseBody
    // @GetMapping("/favoriteList")
    // public String getList(@AuthenticationPrincipal UserDetails userDetails) {
    // User user = userService.getUser(userDetails.getUsername());
    // List<Image> images = favoriteService.getFollowedContentsImages(user);
    // return images.get(0).getPath();
    // }

}
