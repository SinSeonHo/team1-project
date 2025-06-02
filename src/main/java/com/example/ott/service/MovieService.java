package com.example.ott.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Movie;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    // 영화 등록
    public Movie insertMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // 영화 단건 조회
    public Optional<Movie> getMovie(Long mid) {
        return movieRepository.findById(mid);
    }

    // 전체 영화 목록 조회
    public List<Movie> getMovieAll() {
        return movieRepository.findAll();
    }

    // 영화 삭제
    public void deleteMovie(Long mid) {
        movieRepository.deleteById(mid);
    }

    // 영화 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public Movie updateMovie(Movie movie) {
        return movieRepository.save(movie); // ID가 있으면 update
    }
}