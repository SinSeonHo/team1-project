package com.example.ott.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.Game;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.entity.User;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.FollowedContentsRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowedContentsService {

    private final FollowedContentsRepository followedContentsRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ContentsService contentsService;
    private final ContentsRepository contentsRepository;
    private final UserService userService;

    // 팔로우 여부를 확인하는 메서드
    public boolean isFollowed(UserDetails userDetails, String contentsId) {

        // 존재하지 않는 콘텐츠일 경우 처리
        if (!contentsRepository.existsByContentsId(contentsId)) {
            throw new NoSuchElementException("존재하지 않는 콘텐츠입니다.");
        }

        Contents contents = contentsRepository.findById(contentsId).get();
        User user = userService.getUserById(userDetails.getUsername());

        return followedContentsRepository.existsByUserAndContents(user, contents);

    }

    public boolean isFollowed(User user, String contentsId) {

        // 존재하지 않는 콘텐츠일 경우 처리
        if (!contentsRepository.existsByContentsId(contentsId)) {
            throw new NoSuchElementException("존재하지 않는 콘텐츠입니다.");
        }

        Contents contents = contentsRepository.findById(contentsId).get();

        return followedContentsRepository.existsByUserAndContents(user, contents);

    }

    // 팔로우 토글 버튼 누를 시 발동하는 메서드
    public void follow(User user, String contentsId) {

        // 팔로우 여부 확인
        boolean isFollowed = isFollowed(user, contentsId);
        Contents contents = contentsRepository.findById(contentsId).get();

        if (isFollowed) {
            // 팔로우 된 콘텐츠
            FollowedContents followedContents = followedContentsRepository.findByUserAndContents(user, contents);
            followedContentsRepository.delete(followedContents);

        } else {
            // 팔로우 되지 않은 콘텐츠
            FollowedContents followedContents = FollowedContents.builder()
                    .contents(contents)
                    .contentsId(contentsId)
                    .user(user)
                    .build();

            followedContentsRepository.save(followedContents);
        }

    }

    // 해당 유저가 팔로우 한 콘텐츠를 조회하는 리스트
    public List<FollowedContents> getFollowedContentsList(String nickname) {

        User user = userService.getUserByNickname(nickname);

        return followedContentsRepository.findByUser(user);
    }
}
