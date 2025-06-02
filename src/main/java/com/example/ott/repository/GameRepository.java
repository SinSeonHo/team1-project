package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Game;

public interface GameRepository extends JpaRepository<Game, Long> {

}
