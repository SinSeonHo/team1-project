package com.example.ott.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.entity.User;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequestMapping("/favorite")
@RestController
@RequiredArgsConstructor
public class FollowController {
    private final FollowedContentsService followedContentsService;
    private final UserService userService;

    @GetMapping("/toggle")
    public void toggleFavorite(@AuthenticationPrincipal UserDetails userDetails, String contentsId) {
        User user = userService.getUserById(userDetails.getUsername());
        followedContentsService.follow(user, contentsId);
        

    }
    

}
