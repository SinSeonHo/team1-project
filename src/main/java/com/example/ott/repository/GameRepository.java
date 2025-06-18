package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Game;

public interface GameRepository extends JpaRepository<Game, String> {

    @Query(value = "SELECT gid FROM game WHERE gid LIKE 'g_%' ORDER BY TO_NUMBER(SUBSTR(gid, 3)) DESC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    String findLastGameId();

    Optional<Game> findByAppid(String appid);
}
