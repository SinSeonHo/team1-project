package com.example.ott.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Favorite;
import com.example.ott.entity.User;
import com.example.ott.repository.FavoriteRepository;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

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
            favoriteRepository.delete(favorite);

        } else {
            favoriteRepository.save(favorite);
        }
    }

    // favorite 전부를 리스트로 반환 하는데!
    // 영화냐 게임이냐에 따라 Map에 담는걸 분류해야함
    public MultiValueMap<String, Object> favoriteList(User user) {
        MultiValueMap<String, Object> favoriteList = new LinkedMultiValueMap<>();

        List<Favorite> favoriteMovieList = favoriteRepository.findByContentsType(ContentsType.MOVIE);
        List<Favorite> favoriteGameList = favoriteRepository.findByContentsType(ContentsType.GAME);

        favoriteMovieList.stream().forEach(movie -> {
            favoriteList.add("movies", movie);
        });

        favoriteGameList.stream().forEach(game -> {
            favoriteList.add("games", game);
        });

        return favoriteList;
    }
}
