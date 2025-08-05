package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {

}
