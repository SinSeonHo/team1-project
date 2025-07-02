package com.example.ott.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Favorite;
import com.example.ott.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByContentsId(String contentsId);

    // ContentsType 별 분류하기 (영화, 게임)
    List<Favorite> findByUser(User user);

    Favorite findByContentsId(String contentsId);

    Optional<Favorite> findByUserAndContentsId(User user, String contentsId);

    boolean existsByUserAndContentsId(User user, String contentsId);

    long countByContentsId(String contentsId);
}
