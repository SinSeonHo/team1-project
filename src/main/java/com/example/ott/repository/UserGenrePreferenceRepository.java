package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.UserGenrePreference;
import com.example.ott.entity.Genre;
import java.util.List;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

    boolean existsByGenre(Genre genre);

    UserGenrePreference findByGenre(Genre genre);

    @Query("select ugp from UserGenrePreference ugp where ugp.count <= 0 ")
    List<UserGenrePreference> findZeroOrNegativePreferences();
}
