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

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.service.GameService;
import com.example.ott.service.MovieService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

// @RestController
@Controller
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Log4j2
public class GameController {

    private final GameService gameService;

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
        return "ssh_contents/importGameResult"; // templates/importResult.html 로 포워딩
    }

    // game 전체 리스트
    @GetMapping("/list")
    public String getGameList(Model model, PageRequestDTO pageRequestDTO) {
        log.info("gameList 요청 {}", pageRequestDTO);
        List<Game> list = gameService.getGameAll();
        model.addAttribute("games", list);
        return "ssh_contents/gameList";
    }

    // 하나의 game 상세정보
    @GetMapping("/read/{gid}")
    public String getGameInfo(@PathVariable String gid, PageRequestDTO pageRequestDTO, Model model) {
        log.info("영화 상세정보 요청 {}", gid);
        Game game = gameService.getGame(gid).orElseThrow(() -> new RuntimeException("게임 정보 없음"));
        model.addAttribute("gameInfo", game);
        return "ssh_contents/gameInfo";
    }

}