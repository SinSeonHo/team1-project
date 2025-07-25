package com.example.ott.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.service.FavoriteService;
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
    private final FavoriteService favoriteService;

    @GetMapping("/import")
    public String importMovies(Model model) {
        try {
            movieService.importMovies();
            model.addAttribute("message", "영화 데이터 저장 완료!");
        } catch (Exception e) {
            model.addAttribute("message", "에러 발생: " + e.getMessage());
        }

        // DB에 저장된 전체 영화 목록 조회
        List<Movie> movieList = movieService.getMovieAll();
        model.addAttribute("movies", movieList);
        return "ott_contents/importMovieResult"; // templates/importMovieResult.html 로 포워딩
    }

    // movie 전체 리스트
    @GetMapping("/list")
    public String getMovieList(PageRequestDTO pageRequestDTO, Model model) {
        log.info("movieList 요청 {}", pageRequestDTO);
        PageResultDTO<MovieDTO> result = movieService.getSearch(pageRequestDTO);
        model.addAttribute("movies", result.getDtoList());
        return "ott_contents/movieList";
    }

    // 하나의 movie 상세정보
    @GetMapping("/read/{mid}")
    public String getMovieInfo(@PathVariable String mid, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> data = movieService.getMovie(mid);
        Movie movie = (Movie) data.get("movie");
        boolean isFollowed = false;
        isFollowed = favoriteService.isFollowed(userDetails, mid);

        model.addAttribute("content", movie);
        model.addAttribute("replies", data.get("replies"));
        model.addAttribute("isFollowed", isFollowed);
        log.info("로그확인 {}", model);

        return "ott_contents/movieInfo";
    }

    // db상에 int형태로 저장된 상영시간을 n시간 n분형태로 변환하여 반환
    private String convertShowTm(Integer minutes) {
        if (minutes == null || minutes == 0)
            return "상영시간없음";
        int hrs = minutes / 60;
        int mins = minutes % 60;
        return hrs + "시간 " + mins + "분";
    }

}