package com.example.ott.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsGenre;
import com.example.ott.entity.Genre;
import com.example.ott.entity.User;
import com.example.ott.entity.UserGenrePreference;
import com.example.ott.repository.ContentsGenreRepository;
import com.example.ott.repository.UserGenrePreferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserGenrePreferenceService {

    private final ContentsGenreRepository contentsGenreRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;

    // 팔로우한 콘텐츠를 받아 그 콘텐츠들의 장르를 유저 취향에 추가
    public void addUserPreference(User user, Contents contents) {
        // follow한 콘텐츠의 장르 추출
        List<ContentsGenre> contentsGenres = contentsGenreRepository.findByContents(contents);

        contentsGenres.forEach(contentsGenre -> {
            Genre genre = contentsGenre.getGenre();
            if (userGenrePreferenceRepository.existsByGenre(genre)) {
                // 유저 취향에 장르가 이미 있을경우 count + 1
                userGenrePreferenceRepository.save(userGenrePreferenceRepository.findByGenre(genre).addCount());
            } else {
                // 유저 취향에 장르가 존재하지 않을 경우 새로 생성
                UserGenrePreference userGenrePreference = UserGenrePreference.builder()
                        .user(user)
                        .genre(genre)
                        .build();
                try {

                    userGenrePreferenceRepository.save(userGenrePreference);
                } catch (Exception e) {
                    log.info("이 구간에 에러가 있어요! \n userGenrePreference : {}", userGenrePreference);
                }

            }
        });
    }

    public void removePreference(User user, Contents contents) {
        // unfollow한 콘텐츠의 장르 추출
        List<ContentsGenre> contentsGenres = contentsGenreRepository.findByContents(contents);

        contentsGenres.forEach(contentsGenre -> {
            Genre genre = contentsGenre.getGenre();
            if (userGenrePreferenceRepository.existsByGenre(genre)) {
                // 유저 취향에 장르가 이미 있을경우 count - 1
                userGenrePreferenceRepository.save(userGenrePreferenceRepository.findByGenre(genre).minusCount());
            }
            // 유저 취향의 count가 0 이하인 항목들은 삭제
            userGenrePreferenceRepository.findZeroOrNegativePreferences()
                    .forEach(userGenrePreference -> userGenrePreferenceRepository.delete(userGenrePreference));

        });
    }
}
