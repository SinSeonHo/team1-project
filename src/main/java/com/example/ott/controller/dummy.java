package com.example.ott.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.ott.entity.Movie;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
public class dummy {
    private final MovieService movieService;

    @GetMapping("/form")
    public String dummyl(String mid, Model model) {
        Optional<Movie> dto = movieService.getMovie(mid);
        model.addAttribute("dto", dto.get());
        log.info("{} 요청", dto);
        return "form";
    }
}
