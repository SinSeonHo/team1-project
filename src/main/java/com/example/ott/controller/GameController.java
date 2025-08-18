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

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Game;

import com.example.ott.service.FollowedContentsService;

import com.example.ott.entity.Image;

import com.example.ott.service.GameService;
import com.example.ott.service.ImageService;
import com.example.ott.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    private final FollowedContentsService followedContentsService;

    private final ImageService imageService;
    private final ReplyService replyService;

    @GetMapping("/import")
    public String importGames(Model model) {
        try {
            gameService.importGames();
            model.addAttribute("message", "게임 데이터 저장 완료!");
        } catch (Exception e) {
            model.addAttribute("message", "에러 발생: " + e.getMessage());
        }

        // DB에 저장된 전체 게임 목록 조회
        List<Game> gameList = gameService.getGameAll();
        model.addAttribute("games", gameList);
        return "ott_contents/importGameResult";
    }

    // game 전체 리스트
    @GetMapping("/list")
    public String getGameList(Model model, PageRequestDTO pageRequestDTO) {

        PageResultDTO<GameDTO> result = gameService.getSearch(pageRequestDTO);
        model.addAttribute("games", result.getDtoList());
        log.info("game전체리스트 로그로그");
        return "ott_contents/gameList";
    }

    // 하나의 game 상세정보
    @GetMapping("/read/{gid}")
    public String getGameInfo(@PathVariable String gid, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> data = gameService.getGame(gid);
        Game game = (Game) data.get("game");
        boolean isFollowed = false;
        isFollowed = followedContentsService.isFollowed(userDetails, gid);
        System.out.println("게임컨트롤러 상세페이지 로그로그");
        log.info("게임컨트롤러 상세페이지 로그로그{}처음", game);
        // 이미지 및 스크린샷 처리
        Image image = game.getImage(); // Image 객체 얻기
        List<String> screenshots = new ArrayList<>();
        if (image != null && image.getInum() != null) {
            screenshots = imageService.getScreenshotsByImageId(image.getInum());
        }
        log.info("게임컨트롤러 상세페이지 로그로그{}중간", game);
        // 즐겨찾기 여부
        isFollowed = followedContentsService.isFollowed(userDetails, gid);
        // 별점 정보
        List<ReplyDTO> replies = (List<ReplyDTO>) data.get("replies");

        // 별점 정보
        double rating = replyService.rating(replies);

        // 모델에 데이터 추가

        model.addAttribute("gameInfo", game);
        model.addAttribute("replies", replies);
        model.addAttribute("isFollowed", isFollowed);
        model.addAttribute("rating", rating);
        model.addAttribute("screenshotUrls", screenshots);
        log.info("게임컨트롤러 상세페이지 로그로그{}마지막", game);
        return "ott_contents/gameInfo";
    }

}