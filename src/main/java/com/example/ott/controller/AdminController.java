package com.example.ott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GameService gameService;
    private final MovieService movieService;

    @GetMapping("/home")
    public String adminPage() {
        return "admin/admin"; // templates/admin/admin.html 렌더링
    }

    // 관리자 게임 이미지 크롤링
    @PostMapping("/crawl/game")
    @ResponseBody
    public String gameImgCrawl() {
        new Thread(() -> gameService.runPythonGameCrawler()).start();
        System.out.println("게임 크롤링 시작");
        return "게임 이미지 크롤링 백그라운드에서 실행.";
    }

    // 관리자 영화 이미지 크롤링
    @PostMapping("/crawl/movie")
    @ResponseBody
    public String movieImgCrawl() {
        new Thread(() -> movieService.runPythonMovieCrawler()).start();
        System.out.println("영화 크롤링 시작");
        return "영화 이미지 크롤링 백그라운드에서 실행.";
    }

    // 관리자 게임 데이터 수집
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

    // 관리자 영화 데이터 수집
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

    // ※ /admin/report 경로 삭제해서 충돌 방지
}
