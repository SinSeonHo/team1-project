package com.example.ott.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ott.dto.ContentRecommendation;
import com.example.ott.entity.Genre;
import com.example.ott.entity.User;
import com.example.ott.entity.UserGenrePreference;

import jakarta.transaction.Transactional;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

  boolean existsByGenreAndUser(Genre genre, User user);

  @Transactional
  UserGenrePreference findByGenreAndUser(Genre genre, User user);

  // 사용자의 장르 취향의 count가 0이하인 것을 제거하기 위한 조회
  @Query("select ugp from UserGenrePreference ugp where ugp.count <= 0 ")
  List<UserGenrePreference> findZeroOrNegativePreferences();

  /**
   * 사용자(userId)가 팔로우하지 않은 콘텐츠 중,
   * user_genre_preference.count 가중합이 높은 순으로 추천
   */
  @Query(value = """
      WITH
        user_prefs AS (
          SELECT genre_id, count
          FROM user_genre_preference
          WHERE user_id = :userId
        ),
        excluded AS (
          SELECT contents_id
          FROM followed_contents
          WHERE user_id = :userId
        )
      SELECT
        cg.contents_id           AS contentsId,
        SUM(up.count)            AS score
      FROM contents_genre cg
      JOIN user_prefs up
        ON cg.genre_id = up.genre_id
      LEFT JOIN excluded ex
        ON cg.contents_id = ex.contents_id
      WHERE ex.contents_id IS NULL
      GROUP BY cg.contents_id
      ORDER BY score DESC
      """, nativeQuery = true)
  List<ContentRecommendation> recommendByUserPreference(
      @Param("userId") String userId);

}
