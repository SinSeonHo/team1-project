package com.example.ott.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.CountDatasDTO;
import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.entity.Contents;
import com.example.ott.service.ContentsService;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;
import com.example.ott.service.ReplyService;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
public class HomeController {
        private final MovieService movieService;
        private final GameService gameService;
        private final ContentsService contentsService;
        private final UserService userService;
        private final ReplyService replyService;
        private final FollowedContentsService followedContentsService;

        @GetMapping("/")
        public String getHome(Model model, PageRequestDTO requestDTO,
                        @AuthenticationPrincipal UserDetails userDetails) {
                // TOP 10 콘텐츠 들
                
                List<MovieDTO> movieTop10 = movieService.getTop10();

                movieTop10.forEach(movie -> log.info("영화 정보 : {}", movie));
                model.addAttribute("movieTop10", movieTop10);
                List<GameDTO> gameTop10 = gameService.getTop10();
                model.addAttribute("gameTop10", gameTop10);

                // 카운트 데이터 들
                CountDatasDTO countDatasDTO = CountDatasDTO.builder()
                                .contentsCnt(ceilTo10(contentsService.getContentsCnt()))
                                .userCnt(ceilTo10(userService.getUserCnt()))
                                .replyCnt(ceilTo10(replyService.getreplyCnt()))
                                .followedCnt(ceilTo10(followedContentsService.getFollowedContentsCnt()))
                                .build();

                model.addAttribute("countDatasDTO", countDatasDTO);

                // 추천 콘텐츠

                String id = (userDetails != null) ? userDetails.getUsername() : null;
                List<ContentsDTO> recommendContentsDTO = contentsService.getRecommendContents(id);
                model.addAttribute("recommendContentsDTO", recommendContentsDTO);
                // recommendContentsDTO.forEach(dto -> );
                log.info("추천 콘텐츠 사이즈 {}", recommendContentsDTO.size());

                return "/home";
        }

        @GetMapping("/contents")
        public String contents(Model model, PageRequestDTO requestDTO) {
                List<ContentsDTO> genre = contentsService.searchByGenre(requestDTO.getKeyword());
                PageResultDTO<ContentsDTO> result = contentsService.search(requestDTO);
                model.addAttribute("contents", result.getDtoList());
                // model.addAttribute("result", result);
                model.addAttribute("genreContents", genre);
                return "ott_contents/contentList";
        }

        @ResponseBody
        @GetMapping("/auth")
        public Authentication gAuthentication() {
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication authentication = context.getAuthentication();

                return authentication;
        }

        private static long ceilTo10(long v) {
                if (v <= 0)
                        return 0L; // 음수/0이면 0으로
                return (long) (Math.ceil(v / 10.0) * 10); // 10.0으로 나눠서 double 올림 후 10 곱하기
        }
}