package com.example.ott.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GameService gameService;
    private final MovieService movieService;

    @GetMapping("/home")
    public String adminPage() {
        return "/admin/admin"; // templates/admin.html 렌더링
    }

    // 관리자 게임이미지 크롤링
    @PostMapping("/crawl/game")
    @ResponseBody
    public String gameImgCrawl() {
        // 크롤링이 오래 걸리면 아래처럼 백그라운드 스레드로 처리
        new Thread(() -> gameService.runPythonGameCrawler()).start();
        System.out.println("게임크롤링시작");
        return "게임 이미지 크롤링 백그라운드에서 실행.";
    }

    // 관리자 영화이미지 크롤링
    @PostMapping("/crawl/movie")
    @ResponseBody
    public String movieImgCrawl() {
        new Thread(() -> movieService.runPythonMovieCrawler()).start();
        System.out.println("영화크롤링시작");
        return "영화 이미지 크롤링 백그라운드에서 실행.";
    }

    // 관리자 게임데이터 수집
    @PostMapping("/import/games")
    @ResponseBody
    public String importGame() {
        try {
            gameService.importGames();
            return "게임 데이터 저장 완료!";
        } catch (Exception e) {
            return "게임 데이터 저장 실패: " + e.getMessage();
        }
    }

    // 관리자 영화데이터 수집
    @PostMapping("/import/movies")
    @ResponseBody
    public String importMovies() {
        try {
            movieService.importMovies();
            return "영화 데이터 저장 완료!";
        } catch (Exception e) {
            return "영화 데이터 저장 실패: " + e.getMessage();
        }
    }

}
