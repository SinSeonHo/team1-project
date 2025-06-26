package com.example.ott.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByContentsId(String contentsId);

    // ContentsType 별 분류하기 (영화, 게임)
    List<Favorite> findByContentsType(ContentsType contentsType);
}
