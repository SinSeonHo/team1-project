package com.example.ott.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ott.entity.Report;
import com.example.ott.entity.User;
import com.example.ott.entity.Reply;

/**
 * Report 엔티티에 대한 JPA Repository 인터페이스
 * - 기본 CRUD 제공 (JpaRepository 상속)
 * - 상태, 신고자, 댓글 별 조회 및 페이징용 커스텀 쿼리 포함
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

        /**
         * 특정 신고자(User)로 신고 목록 조회
         */
        List<Report> findByReporter(User reporter);

        /**
         * 특정 댓글(Reply)에 대한 신고 목록 조회
         */
        List<Report> findByReply(Reply reply);

        /**
         * 상태(status)로 신고 목록 조회 (예: "PENDING", "DONE")
         */
        List<Report> findByStatus(String status);

        /**
         * 신고자와 상태로 필터링 조회
         */
        List<Report> findByReporterAndStatus(User reporter, String status);

        /**
         * 상태별 페이징 조회
         */
        Page<Report> findByStatus(String status, Pageable pageable);

        /**
         * 전체 페이징 조회
         * JpaRepository 기본 제공 (따로 선언할 필요 없음)
         * Page<Report> findAll(Pageable pageable);
         */

        /**
         * 키워드만 포함 검색 (상태 무관) 페이징
         * reporter.id, reporter.nickname, reason 필드에서 대소문자 구분 없이 검색
         */
        @Query("SELECT r FROM Report r WHERE " +
                        "LOWER(r.reporter.id) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.reporter.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        Page<Report> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

        /**
         * 상태 + 키워드 모두 포함 검색 페이징
         */
        @Query("SELECT r FROM Report r WHERE r.status = :status AND (" +
                        "LOWER(r.reporter.id) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.reporter.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(r.reason) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Report> findByStatusAndKeyword(@Param("status") String status,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}
