package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ott.entity.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
