package com.example.ott.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MovieService movieService;
    private final GameService gameService;

    @GetMapping("/")
    public String getHome(Model model) {
        PageResultDTO<MovieDTO> list = movieService.getList(PageRequestDTO.builder().size(6).build());
        PageResultDTO<GameDTO> gamelist = gameService.getGameRequest(PageRequestDTO.builder().size(6).build());
        model.addAttribute("movies", list.getDtoList());
        model.addAttribute("games", gamelist.getDtoList());
        return "index";
    }

    @ResponseBody
    @GetMapping("/auth")
    public Authentication getAuth() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication;
    }
}
