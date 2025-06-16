package com.example.ott.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.service.MovieService;
import com.example.ott.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.bind.annotation.RequestParam;

// @RestController
@Controller
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Log4j2
public class MovieController {

    private final MovieService movieService;
    private final ReplyService replyService;

    @GetMapping("/import")
    public String importMovies(Model model) {
        try {
            movieService.importMovies();
            model.addAttribute("message", "저장 완료!");
        } catch (Exception e) {
            model.addAttribute("message", "에러 발생: " + e.getMessage());
        }

        // DB에 저장된 전체 게임 목록 조회
        List<Movie> movieList = movieService.getMovieAll();
        model.addAttribute("movies", movieList);
        return "ssh_contents/importMovieResult"; // templates/importMovieResult.html 로 포워딩
    }

    // movie 전체 리스트
    @GetMapping("/list")
    public String getMovieList(Model model, PageRequestDTO pageRequestDTO) {
        log.info("movieList 요청 {}", pageRequestDTO);
        List<Movie> list = movieService.getMovieAll();
        model.addAttribute("movies", list);
        return "ssh_contents/movieList";
    }

    // 하나의 movie 상세정보
    @GetMapping("/read/{mid}")
    public String getMovieInfo(@PathVariable String mid, PageRequestDTO pageRequestDTO, Model model) {
        log.info("영화 상세정보 요청(댓글 포함) {}", mid);
        Movie movie = movieService.getMovie(mid).orElseThrow(() -> new RuntimeException("영화 정보 없음"));
        List<ReplyDTO> replies = replyService.movieReplies(mid);
        model.addAttribute("movieInfo", movie);
        model.addAttribute("replies", replies);
        return "ssh_contents/movieInfo";
    }

}