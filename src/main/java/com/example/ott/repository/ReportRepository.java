package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.ReplyReport;
import com.example.ott.entity.Reply;
import com.example.ott.entity.User;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReplyReport, Long> {

    // 이미 해당 댓글에 대해 사용자가 신고했는지 여부
    boolean existsByCommentAndReporter(Reply reply, User reporter);

    // 특정 댓글에 대한 모든 신고 내역 조회
    List<ReplyReport> findByComment(Reply reply);
}
