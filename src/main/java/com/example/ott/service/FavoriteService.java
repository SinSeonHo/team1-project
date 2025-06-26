package com.example.ott.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Favorite;
import com.example.ott.entity.Game;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.entity.User;
import com.example.ott.repository.FavoriteRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    // 팔로우(토글방식)
    public void follow(User user, String contentsId) {
        // TODO: Contents의 follow Cnt +-1 하는거 추가해야함

        // favorite이 이미 존재할경우 unFollow

        if (favoriteRepository.existsByContentsId(contentsId)) {
            Favorite targetFavorite = favoriteRepository.findByContentsId(contentsId);
            favoriteRepository.delete(targetFavorite);
            return;
        }

        String contentsType = contentsId.split("_")[0];
        Favorite favorite = null;
        switch (contentsType) {
            case "m":
                favorite = Favorite.builder()
                        .user(user)
                        .contentsType(ContentsType.MOVIE)
                        .contentsId(contentsId)
                        .build();

                //
                Movie movie = movieRepository.findById(contentsId).get();
                movie.setFollowcnt((int) favoriteRepository.countByContentsId(contentsId));
                movieRepository.save(movie);
                break;

            case "g":
                favorite = Favorite.builder()
                        .user(user)
                        .contentsType(ContentsType.GAME)
                        .contentsId(contentsId)
                        .build();

                Game game = gameRepository.findById(contentsId).get();
                game.setFollowcnt((int) favoriteRepository.countByContentsId(contentsId));
                gameRepository.save(game);
                break;

            default:
                return;

        }
        favoriteRepository.save(favorite);
    }

    // 특정 유저가 팔로우하여 추가한 favorite Contents들을 리스트로 반환
    public List<Image> getFollowedContentsImages(String id) {
        User user = userRepository.findById(id).get();
        List<Favorite> favoriteList = favoriteRepository.findByUser(user); // 존재하지 않을 경우 기능 이따가 추가
        if (favoriteList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Image> favoriteContentsImages = new ArrayList<>();
        favoriteList.forEach(favorite -> {
            switch (favorite.getContentsType()) {
                case MOVIE:
                    Movie movie = movieRepository.findById(favorite.getContentsId()).get();
                    favoriteContentsImages.add(movie.getImage());
                    break;

                case GAME:
                    Game game = gameRepository.findById(favorite.getContentsId()).get();
                    favoriteContentsImages.add(game.getImage());
                    break;

                default:
                    break;
            }
        });
        return favoriteContentsImages;
    }

    // 현재 로그인 한 유저가 해당 콘텐츠를 팔로우 했는지 확인
    public boolean isFollowed(UserDetails userDetails, String contentsId) {
        try {
            User user = userRepository.findById(userDetails.getUsername()).get();

            return favoriteRepository.existsByUserAndContentsId(user, contentsId);
        } catch (Exception e) {
            return false;
        }
    }
}
