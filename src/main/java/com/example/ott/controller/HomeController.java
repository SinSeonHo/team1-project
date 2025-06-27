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

import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MovieService movieService;
    private final GameService gameService;

    @GetMapping("/")
    public String getHome(Model model, PageRequestDTO requestDTO) {
        List<MovieDTO> movielist = movieService.getRandom(3);
        model.addAttribute("movies", movielist);
        List<GameDTO> gamelist = gameService.getRandom(6);
        model.addAttribute("games", gamelist);

        return "index";
    }

}