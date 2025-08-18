package com.example.ott.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ContentRecommendation;
import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsGenre;
import com.example.ott.entity.Genre;
import com.example.ott.entity.User;
import com.example.ott.entity.UserGenrePreference;
import com.example.ott.repository.ContentsGenreRepository;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.UserGenrePreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGenrePreferenceService {

    private final ContentsRepository contentsRepository;
    private final ContentsGenreRepository contentsGenreRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;

    // 팔로우한 콘텐츠를 받아 그 콘텐츠들의 장르를 유저 취향에 추가
    public void addUserPreference(User user, Contents contents) {
        // follow한 콘텐츠의 장르 추출 그 유저가 팔로우한!
        List<ContentsGenre> contentsGenres = contentsGenreRepository.findByContents(contents);

        contentsGenres.forEach(contentsGenre -> {

            Genre genre = contentsGenre.getGenre();
            if (userGenrePreferenceRepository.existsByGenreAndUser(genre, user)) {

                // 유저 취향에 장르가 이미 있을경우 count + 1
                UserGenrePreference userGenrePreference = userGenrePreferenceRepository.findByGenreAndUser(genre, user);
                userGenrePreference.addCount();
                userGenrePreferenceRepository.save(userGenrePreference);
            } else {
                // 유저 취향에 장르가 존재하지 않을 경우 새로 생성
                UserGenrePreference userGenrePreference = UserGenrePreference.builder()
                        .user(user)
                        .genre(genre)
                        .genreName(genre.getGenreName())
                        .build();
                try {

                    userGenrePreferenceRepository.save(userGenrePreference);
                } catch (Exception e) {

                }

            }
        });
    }

    // 언팔로우한 콘텐츠를 받아 그 콘텐츠들의 장르를 유저 취향에서 제거
    public void removeUserPreference(User user, Contents contents) {
        // unfollow한 콘텐츠의 장르 추출
        List<ContentsGenre> contentsGenres = contentsGenreRepository.findByContents(contents);

        contentsGenres.forEach(contentsGenre -> {
            Genre genre = contentsGenre.getGenre();
            if (userGenrePreferenceRepository.existsByGenreAndUser(genre, user)) {
                // 유저 취향에 장르가 이미 있을경우 count - 1
                userGenrePreferenceRepository
                        .save(userGenrePreferenceRepository.findByGenreAndUser(genre, user).minusCount());
            }
            // 유저 취향의 count가 0 이하인 항목들은 삭제
            userGenrePreferenceRepository.findZeroOrNegativePreferences()
                    .forEach(userGenrePreference -> userGenrePreferenceRepository.delete(userGenrePreference));

        });
    }

    // user의 장르 취향에 최대한 적합한 콘텐츠들 조회
    public List<Contents> getRecommendationContents(String userId) {
        List<ContentRecommendation> recommendedContents = userGenrePreferenceRepository
                .recommendByUserPreference(userId);
        // List<ContentsRecommendation> -> List<Contents>로 변경 + contentsRecommendation의
        // score가 (임시)2이하이면 걸러
        List<Contents> result = recommendedContents.stream()
                .filter(cr -> cr.getScore() >= 2)
                .map(cr -> contentsRepository.findById(cr.getContentsId()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        return result;
    }
}
