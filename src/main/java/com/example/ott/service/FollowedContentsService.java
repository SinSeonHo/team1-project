package com.example.ott.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.ott.dto.FollowedContentsDTO;
import com.example.ott.entity.Contents;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.User;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.FollowedContentsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class FollowedContentsService {

    private final FollowedContentsRepository followedContentsRepository;
    private final ContentsRepository contentsRepository;
    private final UserService userService;
    private final UserGenrePreferenceService userGenrePreferenceService;

    // 팔로우 여부를 확인하는 메서드
    public boolean isFollowed(UserDetails userDetails, String contentsId) {

        if (userDetails == null) {
            return false;
        }
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
        log.info("팔로우 여부 확인 {}", isFollowed);
        if (isFollowed) {
            // 팔로우 된 콘텐츠
            FollowedContents followedContents = followedContentsRepository.findByUserAndContents(user, contents);
            userGenrePreferenceService.removeUserPreference(user, contents);

            // 팔로우 카운트 감소
            contents.minusFollowCnt();
            contentsRepository.save(contents);

            followedContentsRepository.delete(followedContents);

        } else {
            // 팔로우 되지 않은 콘텐츠
            FollowedContents followedContents = FollowedContents.builder()
                    .contents(contents)
                    .user(user)
                    .build();

            // 팔로우 카운트 증가
            contents.addFollowCnt();
            contentsRepository.save(contents);

            followedContentsRepository.save(followedContents);

            try {

                userGenrePreferenceService.addUserPreference(user, contents);

            } catch (Exception e) {
                log.info("이 구간에서 문제가 있어요. \n user : {} \n contents {}", user, contents);
                e.printStackTrace();
            }
        }

    }

    // 해당 유저가 팔로우 한 콘텐츠를 조회하는 리스트
    public List<FollowedContentsDTO> getFollowedContentsList(String nickname) {

        User user = userService.getUserByNickname(nickname);

        List<FollowedContents> followedContentsList = followedContentsRepository.findByUser(user);
        List<FollowedContentsDTO> followedContentsDTOs = new ArrayList<>();
        followedContentsList.forEach(followedContents -> {
            String imageUrl = "";
            switch (followedContents.getContents().getContentsType()) {
                case MOVIE:
                    imageUrl = followedContents.getContents().getMovie().getImage().getPath();
                    break;
                case GAME:
                    imageUrl = followedContents.getContents().getGame().getImage().getPath();
                    break;

                default:
                    break;
            }

            FollowedContentsDTO followedContentsDTO = FollowedContentsDTO.builder()
                    .contentsId(followedContents.getContents().getContentsId())
                    .imageUrl(imageUrl)
                    .userId(followedContents.getUser().getId())
                    .contentsType(followedContents.getContents().getContentsType())
                    .build();

            followedContentsDTOs.add(followedContentsDTO);
        });

        return followedContentsDTOs;
    }

    public Page<FollowedContentsDTO> getFollowedContentsList(String nickname, Pageable pageable) {

        // User user = userService.getUserByNickname(nickname);

        Page<FollowedContents> followedContentsList = followedContentsRepository.findByUserId(nickname, pageable);
        log.info("FollowedContents 조회 결과 수: {}", followedContentsList.getTotalElements());
        Page<FollowedContentsDTO> followedContentsDTOs = followedContentsList.map(followedContents -> {
            String imageUrl = "";
            switch (followedContents.getContents().getContentsType()) {
                case MOVIE:
                    imageUrl = followedContents.getContents().getMovie().getImage().getPath();
                    break;
                case GAME:
                    imageUrl = followedContents.getContents().getGame().getImage().getPath();
                    break;

                default:
                    break;
            }

            return FollowedContentsDTO.builder()
                    .contentsId(followedContents.getContents().getContentsId())
                    .imageUrl(imageUrl)
                    .userId(followedContents.getUser().getId())
                    .contentsType(followedContents.getContents().getContentsType())
                    .build();
        });

        return followedContentsDTOs;
    }

}
