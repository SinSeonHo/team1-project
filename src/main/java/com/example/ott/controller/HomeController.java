package com.example.ott.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MovieService movieService;
    private final GameService gameService;

    @GetMapping("/")
    public String getHome(Model model, PageRequestDTO pageRequestDTO) {
        List<Movie> list = movieService.getMovieRank(6);
        List<Game> gamelist = gameService.getGameRank(6);
        model.addAttribute("movies", list);
        model.addAttribute("games", gamelist);
        return "ott_contents/main";
    }

    @ResponseBody
    @GetMapping("/auth")
    public Authentication getAuth() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        return authentication;
    }
}