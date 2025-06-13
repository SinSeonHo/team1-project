package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Game;

public interface GameRepository extends JpaRepository<Game, String> {

    // @Query("SELECT g.gid FROM Game g WHERE g.gid LIKE 'g_%' ORDER BY
    // CAST(SUBSTRING(g.gid, 3) AS int) DESC LIMIT 1")
    // String findLastGameId();

    // Optional<Game> findByAppId(String appId);

    // @Query(value = "SELECT gid FROM game WHERE gid LIKE 'g_%' ORDER BY
    // CAST(SUBSTRING(gid, 3) AS SIGNED) DESC LIMIT 1", nativeQuery = true)
    // String findLastGameId();

    @Query(value = "SELECT gid FROM game WHERE gid LIKE 'g_%' ORDER BY TO_NUMBER(SUBSTR(gid, 3)) DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    String findLastGameId();

    Optional<Game> findByAppid(String appid);
}
