package com.example.ott.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

// import com.example.ott.entity.Movie;
import com.example.ott.entity.Image;
import com.example.ott.repository.total.totalImageRepository;

public interface ImageRepository extends JpaRepository<Image, Long>, totalImageRepository {
    // movie번호를 기준으로 이미지 제거

    // @Modifying // delete, update 시 반드시 작성
    // @Query("DELETE FROM MovieImage mi WHERE mi.movie = :movie")
    // void deleteByMovie(Movie movie);

    // // nativeQuery = true : sql 쿼리문사용
    // @Query(value = "SELECT * FROM MOVIE_IMAGE mi WHERE mi.path =
    // to_char(sysdate-1, 'yyyy\\mm\\dd')", nativeQuery = true)
    // List<MovieImage> getOldImages();
}
