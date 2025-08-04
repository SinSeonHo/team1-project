package com.example.ott.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ott.dto.ContentRecommendation;
import com.example.ott.entity.Genre;
import com.example.ott.entity.UserGenrePreference;

public interface UserGenrePreferenceRepository extends JpaRepository<UserGenrePreference, Long> {

    boolean existsByGenre(Genre genre);

    UserGenrePreference findByGenre(Genre genre);

    // 사용자의 장르 취향의 count가 0이하인 것을 제거하기 위한 조회
    @Query("select ugp from UserGenrePreference ugp where ugp.count <= 0 ")
    List<UserGenrePreference> findZeroOrNegativePreferences();

    /**
     * 사용자(userId)가 팔로우하지 않은 콘텐츠 중,
     * user_genre_preference.count 가중합이 높은 순으로 추천
     */
    @Query(value = """
            SELECT
              cg.contents_id   AS contentsId,
              SUM(ugp.count)   AS score
            FROM contents_genre cg
            JOIN user_genre_preference ugp
              ON cg.genre_id = ugp.genre_id
            WHERE ugp.user_id = :userId
              AND cg.contents_id NOT IN (
                SELECT fc.contents_id
                FROM followed_contents fc
                WHERE fc.user_id = :userId
              )
            GROUP BY cg.contents_id
            ORDER BY score DESC
            """, nativeQuery = true)
    List<ContentRecommendation> recommendByUserPreference(
            @Param("userId") String userId);

}
