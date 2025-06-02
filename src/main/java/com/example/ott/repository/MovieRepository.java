package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

}
