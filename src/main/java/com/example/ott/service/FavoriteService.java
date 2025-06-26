package com.example.ott.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Favorite;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.User;
import com.example.ott.repository.FavoriteRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;

    // 찜하기(토글방식)
    public void toggleFavorite(User user, String contentsId) {

        String contentsType = contentsId.split("_")[0];
        Favorite favorite = null;

        switch (contentsType) {
            case "m":
                favorite = Favorite.builder()
                        .user(user)
                        .contentsType(ContentsType.MOVIE)
                        .contentsId(contentsId)
                        .build();

                break;

            case "g":
                favorite = Favorite.builder()
                        .user(user)
                        .contentsType(ContentsType.GAME)
                        .contentsId(contentsId)
                        .build();
                break;

            default:
                break;
        }

        // favorite이 이미 존재할경우 삭제, 아닐경우 추가
        if (favoriteRepository.existsByContentsId(contentsId)) {
            Favorite deleteFavorite = favoriteRepository.findById(favorite.getId()).get();
            favoriteRepository.delete(deleteFavorite);

        } else {
            favoriteRepository.save(favorite);
        }
    }

    // 입력한 콘텐츠 타입의 값에 따라 List<Movie or Game> 을 반환
    public List<Object> getFavoriteContentsList(User user, ContentsType contentsType) {
        List<Object> contentsList = new ArrayList<>();
        switch (contentsType) {
            case MOVIE:
                List<Favorite> favoriteMovieList = favoriteRepository.findByContentsType(ContentsType.MOVIE);

                // favorite contents가 존재하지 않을 경우 널 반환
                if (favoriteMovieList.isEmpty()) {
                    return null;
                } else {
                    favoriteMovieList.forEach(favorite -> {
                        Movie movie = movieRepository.findById(favorite.getContentsId()).get();
                        contentsList.add(movie);
                    });

                }
                break;
            case GAME:
                List<Favorite> favoriteGameList = favoriteRepository.findByContentsType(ContentsType.GAME);

                if (favoriteGameList.isEmpty()) {
                    return null; //
                } else {
                    favoriteGameList.forEach(favorite -> {
                        Game game = gameRepository.findById(favorite.getContentsId()).get();
                        contentsList.add(game);
                    });

                }
                break;

            default:
                break;
        }

        return contentsList;
    }
}
