package com.example.ott.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.ImageService;
import com.example.ott.service.MovieService;
import com.example.ott.service.ReplyService;

import lombok.RequiredArgsConstructor;

@Controller

@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    private final FollowedContentsService followedContentsService;

    private final ImageService imageService;
    private final ReplyService replyService;

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
        return "ott_contents/importMovieResult";
    }

    // movie 전체 리스트
    @GetMapping("/list")
    public String getMovieList(PageRequestDTO pageRequestDTO, Model model) {
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
        isFollowed = followedContentsService.isFollowed(userDetails, mid);

        // 즐겨찾기 여부
        isFollowed = followedContentsService.isFollowed(userDetails, mid);
        List<ReplyDTO> replies = (List<ReplyDTO>) data.get("replies");

        // 별점 정보
        double rating = replyService.rating(replies);

        // 이미지 및 스크린샷 처리
        Image image = movie.getImage(); // Image 객체 얻기
        List<String> screenshots = new ArrayList<>();
        if (image != null && image.getInum() != null) {
            screenshots = imageService.getScreenshotsByImageId(image.getInum());
        }

        // 모델에 데이터 추가
        model.addAttribute("movieInfo", movie);
        model.addAttribute("replies", replies);
        model.addAttribute("isFollowed", isFollowed);
        model.addAttribute("screenshotUrls", screenshots);
        model.addAttribute("rating", rating);

        return "ott_contents/movieInfo";
    }

}