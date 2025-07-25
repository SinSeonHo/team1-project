package com.example.ott.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;

    // contents ID(gid or mid)를 통해 일치하는 contents 조회 ====== followed한 contents를 조회할때
    // 사용 예정
    public Contents getContents(String contentsId, ContentsType contentsType) {
        switch (contentsType) {
            case MOVIE:
                Movie movie = movieRepository.findByMovieCd(contentsId).get();
                return contentsRepository.findByMovie(movie);

            case GAME:
                Game game = gameRepository.findByAppid(contentsId).get();
                return contentsRepository.findByGame(game);

            default:
                throw new IllegalArgumentException("해당 contents를 찾을 수 없습니다.");

        }
    }

}
