package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.UserGenrePreference;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

}
