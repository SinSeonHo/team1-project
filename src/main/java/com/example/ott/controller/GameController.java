package com.example.ott.controller;

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
import com.example.ott.service.FavoriteService;
import com.example.ott.service.GameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// @RestController
@Controller
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Log4j2
public class GameController {

    private final GameService gameService;
    private final FavoriteService favoriteService;

    @GetMapping("/import")
    public String importGame(Model model) {
        try {
            gameService.importGames();
            model.addAttribute("message", "게임 데이터 저장 완료!");
        } catch (Exception e) {
            model.addAttribute("message", "에러 발생: " + e.getMessage());
        }

        // DB에 저장된 전체 게임 목록 조회
        List<Game> gameList = gameService.getGameAll();
        model.addAttribute("games", gameList);
        return "ott_contents/importGameResult"; // templates/importResult.html 로 포워딩
    }

    // game 전체 리스트
    @GetMapping("/list")
    public String getGameList(Model model, PageRequestDTO pageRequestDTO) {
        log.info("gameList 요청 {}", pageRequestDTO);
        // List<Game> list = gameService.getGameAll();
        PageResultDTO<GameDTO> result = gameService.getSearch(pageRequestDTO);
        model.addAttribute("games", result.getDtoList());
        return "ott_contents/gameList";
    }

    // 하나의 game 상세정보
    @GetMapping("/read/{gid}")
    public String getGameInfo(@PathVariable String gid, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> data = gameService.getGame(gid);
        Game game = (Game) data.get("game");
        boolean isFollowed = false;
        isFollowed = favoriteService.isFollowed(userDetails, gid);
        // 별점 정보
        List<ReplyDTO> replies = (List<ReplyDTO>) data.get("replies");
        int rating = 0;
        if (replies.size() != 0) {
            for (ReplyDTO dto : replies) {
                rating += dto.getRate();
            }
            rating = rating / replies.size();
        }
        model.addAttribute("gameInfo", game);
        model.addAttribute("replies", replies);
        model.addAttribute("isFollowed", isFollowed);
        model.addAttribute("rating", rating);
        log.info("로그확인 {}", model);

        return "ott_contents/gameInfo";
    }

}