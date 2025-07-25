package com.example.ott.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.ContentsType;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.User;

public interface FollowedContentsRepository extends JpaRepository<FollowedContents, Long> {

    boolean existsByContentsId(String contentsId);

    // ContentsType 별 분류하기 (영화, 게임)
    List<FollowedContents> findByUser(User user);

    FollowedContents findByContentsId(String contentsId);

    Optional<FollowedContents> findByUserAndContentsId(User user, String contentsId);

    boolean existsByUserAndContentsId(User user, String contentsId);

    long countByContentsId(String contentsId);
}
