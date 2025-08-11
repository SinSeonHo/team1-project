package com.example.ott.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsGenre;
import com.example.ott.entity.Genre;
import com.example.ott.repository.ContentsGenreRepository;
import com.example.ott.repository.ContentsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentsGenreService {
    private final ContentsGenreRepository contentsGenreRepository;
    private final ContentsRepository contentsRepository;

    private final GenreService genreService;

    // ContentsGenre Table에 해당 콘텐츠 장르 추가
    public void insertContentsGenre(String contentsId, List<String> genreNames) {
        // 콘텐츠 조회
        Contents contents = contentsRepository.findByContentsId(contentsId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 콘텐츠 입니다."));

        // List<String> -> List<Genre>
        List<Genre> genres = genreService.toGenres(genreNames);

        // ContentsGenre 추가
        genres.forEach(genre -> {

            ContentsGenre contentsGenre = ContentsGenre.builder()
                    .contents(contents)
                    .genre(genre)
                    .genreName(genre.getGenreName())
                    .build();

            contentsGenreRepository.save(contentsGenre);
        });
    }

}
