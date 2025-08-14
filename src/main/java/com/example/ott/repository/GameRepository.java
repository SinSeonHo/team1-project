package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Game;
import com.example.ott.repository.search.GameSearchRepository;

public interface GameRepository extends JpaRepository<Game, String>, GameSearchRepository {

    @Query(value = "SELECT gid FROM game WHERE gid LIKE 'g_%' ORDER BY CAST(SUBSTRING(gid, 3) AS UNSIGNED) DESC LIMIT 1", nativeQuery = true)
    String findLastGameId();

    Optional<Game> findByAppid(String appid);
}
