package com.example.ott.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsGenre;

public interface ContentsGenreRepository extends JpaRepository<ContentsGenre, Long> {
    List<ContentsGenre> findByContents(Contents contents);

}
