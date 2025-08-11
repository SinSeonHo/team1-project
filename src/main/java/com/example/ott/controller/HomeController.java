package com.example.ott.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.service.ContentsService;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;
import com.example.ott.type.ContentType;

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

        @GetMapping("/contents")
        public String contents(Model model, PageRequestDTO requestDTO) {
                PageResultDTO<ContentsDTO> result = contentsService.search(requestDTO);
                model.addAttribute("contents", result.getDtoList());
                return "ott_contents/contentList";
        }

        @ResponseBody
        @GetMapping("/auth")
        public Authentication gAuthentication() {
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication authentication = context.getAuthentication();

                return authentication;
        }

}