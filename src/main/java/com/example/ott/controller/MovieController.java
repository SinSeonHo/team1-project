package com.example.ott.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/import")
    public ResponseEntity<String> importMovies() {
        try {
            movieService.importMovies();
            return ResponseEntity.ok("저장 완료!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("에러 발생: " + e.getMessage());
        }
    }
}