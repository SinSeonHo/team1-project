package com.example.ott.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ott.entity.Reply;
import com.example.ott.entity.Report;
import com.example.ott.entity.User;
import com.example.ott.type.Reason;
import com.example.ott.type.Status;

public interface ReportRepository extends JpaRepository<Report, Long> {

  /** 단일 상태 조회 */
  @EntityGraph(attributePaths = { "reporter", "reply" })
  List<Report> findByStatus(Status status);

  /** 신고자 + 상태 조회 */
  @EntityGraph(attributePaths = { "reporter", "reply" })
  List<Report> findByReporterAndStatus(User reporter, Status status);

  // --- 공통 목록 (N+1 방지) ---
  @EntityGraph(attributePaths = { "reporter", "reply" })
  Page<Report> findAll(Pageable pageable);

  // --- 다중 필터 (서비스에서 null/empty 제거/분기 보장) ---
  @EntityGraph(attributePaths = { "reporter", "reply" })
  @Query("""
      select r
      from Report r
      where r.reason in :reasons
        and r.status in :statuses
      """)
  Page<Report> findAllByFilters(@Param("reasons") Collection<Reason> reasons,
      @Param("statuses") Collection<Status> statuses,
      Pageable pageable);

  // 분기 호출용(하나만 필터일 때)
  @EntityGraph(attributePaths = { "reporter", "reply" })
  Page<Report> findByReasonIn(Collection<Reason> reasons, Pageable pageable);

  @EntityGraph(attributePaths = { "reporter", "reply" })
  Page<Report> findByStatusIn(Collection<Status> statuses, Pageable pageable);

  @EntityGraph(attributePaths = { "reporter", "reply" })
  Page<Report> findByStatus(Status status, Pageable pageable);

  List<Report> findByReply(Reply reply);

  List<Report> findByReporter(User reporter);
}