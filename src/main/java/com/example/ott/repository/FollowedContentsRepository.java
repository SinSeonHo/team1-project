package com.example.ott.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Contents;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.User;

public interface FollowedContentsRepository extends JpaRepository<FollowedContents, Long> {

    boolean existsByContents(Contents contents);

    // ContentsType 별 분류하기 (영화, 게임)
    List<FollowedContents> findByUser(User user);

    Page<FollowedContents> findByUserId(String userId, Pageable pageable);

    FollowedContents findByUserAndContents(User user, Contents contents); // 사용

    boolean existsByUserAndContents(User user, Contents contents); // 사용

    long countByContents(Contents contents); // 사용

}
