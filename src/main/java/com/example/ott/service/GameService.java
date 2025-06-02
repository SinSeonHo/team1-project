package com.example.ott.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Game;
import com.example.ott.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    // 게임 등록
    public Game insertGame(Game game) {
        return gameRepository.save(game);
    }

    // 게임 단건 조회
    public Optional<Game> getGame(Long gid) {
        return gameRepository.findById(gid);
    }

    // 전체 게임 목록 조회
    public List<Game> getGameAll() {
        return gameRepository.findAll();
    }

    // 게임 삭제
    public void deleteGame(Long gid) {
        gameRepository.deleteById(gid);
    }

    // 게임 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public Game updateGame(Game game) {
        return gameRepository.save(game); // ID가 있으면 update
    }
}