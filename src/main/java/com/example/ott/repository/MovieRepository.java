package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, String> {

    @Query("SELECT m.mid FROM Movie m WHERE m.mid LIKE 'm_%' ORDER BY CAST(SUBSTRING(m.mid, 3) AS int) DESC LIMIT 1")
    String findLastMovieId();

    Optional<Movie> findByMovieCd(String movieCd);
}
