package com.example.ott.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Movie;
import com.example.ott.repository.search.MovieSearch;

public interface MovieRepository extends JpaRepository<Movie, String>, MovieSearch {

    @Query("SELECT m.mid FROM Movie m WHERE m.mid LIKE 'm_%' ORDER BY CAST(SUBSTRING(m.mid, 3) AS int) DESC LIMIT 1")
    String findLastMovieId();

    Optional<Movie> findByMovieCd(String movieCd);

    // 랭킹 낮은 순으로 10개
    List<Movie> findTop10ByOrderByRankingAsc();
}