package com.example.ott.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Genre;
import com.example.ott.repository.GenreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    // List<String>의 장르명을 List<Genre>의 장르로 변경
    public List<Genre> toGenres(List<String> genreName) {
        List<Genre> genres = genreName.stream().map(name -> {
            Genre genre;
            if (!genreRepository.findByGenreName(name).isEmpty()) {
                // 장르가 이미 존재할 경우 불러오기
                genre = genreRepository.findByGenreName(name).get();
            } else {
                // 장르가 존재하지 않을 경우 추가
                genre = Genre.builder()
                        .genreName(name)
                        .build();
                genre = genreRepository.save(genre);
            }
            return genre;
        }).collect(Collectors.toList());
        return genres;
    }
}
