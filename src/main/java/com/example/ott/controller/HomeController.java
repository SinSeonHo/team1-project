package com.example.ott.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.service.ContentsService;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
        private final MovieService movieService;
        private final GameService gameService;
        private final ContentsService contentsService;

        @GetMapping("/")
        public String getHome(Model model, PageRequestDTO requestDTO) {
                List<MovieDTO> movielist = movieService.getRandom(10);
                model.addAttribute("movies", movielist);
                List<GameDTO> gamelist = gameService.getRandom(6);
                model.addAttribute("games", gamelist);

                return "index";
        }
}