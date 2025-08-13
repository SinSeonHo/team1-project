package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Contents;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.search.ContentsSearchRepository;

public interface ContentsRepository extends JpaRepository<Contents, String>, ContentsSearchRepository {

    boolean existsByContentsId(String contentsId);

    Contents findByGame(Game game);

    Contents findByMovie(Movie movie);

    Optional<Contents> findByContentsId(String contentsId);

    @Query(value = "SELECT * FROM contents ORDER BY DBMS_RANDOM.VALUE FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<Contents> pickRandom();

}
