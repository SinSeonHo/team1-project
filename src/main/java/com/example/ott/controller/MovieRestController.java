package com.example.ott.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.entity.Movie;
import com.example.ott.service.FavoriteService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Log4j2
public class MovieRestController {

    private final MovieService movieService;
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
    public PageResultDTO<MovieDTO> getMovieList(PageRequestDTO pageRequestDTO) {
        log.info("movieList 요청 {}", pageRequestDTO);
        PageResultDTO<MovieDTO> result = movieService.getSearch(pageRequestDTO);
        return result;
    }

    // 하나의 movie 상세정보
    @GetMapping("/read/{mid}")
    public MovieDTO getMovieInfo(@PathVariable String mid,
            @AuthenticationPrincipal UserDetails userDetails) {
        MovieDTO dto = movieService.restMovie(mid);
        boolean isFollowed = favoriteService.isFollowed(userDetails, mid);
        dto.setFollow(isFollowed);

        log.info("movie 상세정보 요청 {}", dto);
        return dto;
    }

}