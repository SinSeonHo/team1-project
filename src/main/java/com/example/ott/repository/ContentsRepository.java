package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Contents;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;

import java.util.List;

public interface ContentsRepository extends JpaRepository<Contents, String> {

    Contents findByGame(Game game);

    Contents findByMovie(Movie movie);

}
