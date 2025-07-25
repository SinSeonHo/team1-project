package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Contents;

public interface ContentsRepository extends JpaRepository<Contents, Long> {

}
