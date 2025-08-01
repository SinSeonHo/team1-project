package com.example.ott.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.service.ContentsService;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class ContentsRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ContentsRepository contentsRepository;

    @Autowired
    private ContentsService contentsService;

    // 영화 1개 mid로 조회하기
    @Transactional
    @Test
    public void getOneMovieTest() {
        Optional<Movie> movie = movieRepository.findById("m_1");
        System.out.println(movie);
    }

    // 영화 전체리스트 조회하기
    @Test
    public void getAllMoviesTest() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            System.out.println(movie);
        }
    }

    // 게임 1개 gid로 조회하기
    @Test
    public void getOneGameTest() {
        Optional<Game> game = gameRepository.findById("g_1");
        System.out.println(game);
    }

    @Test
    public void getAllGamesTest() {
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            System.out.println(game);
        }
    }

    @Test
    public void addContentsTest() {
        // 영화 -> 콘텐츠 테이블에 추가
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            ContentsDTO contentsDTO = ContentsDTO.builder()
                    .contentsId(movie.getMid())
                    .title(movie.getTitle())
                    .contentsType(ContentsType.MOVIE)
                    .genres(movie.getGenres())
                    .build();
            contentsService.insertContents(contentsDTO);
        }

        // 게임 -> 콘텐츠 테이블에 추가
        List<Game> games = gameRepository.findAll();
        games.forEach(game -> {
            ContentsDTO contentsDTO = ContentsDTO.builder()
                    .contentsId(game.getGid())
                    .title(game.getTitle())
                    .contentsType(ContentsType.GAME)
                    .genres(game.getGenres())
                    .build();
            contentsService.insertContents(contentsDTO);

        });
    }
}
