package com.example.ott.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ott.entity.ReplyReport;
import com.example.ott.entity.Reply;
import com.example.ott.entity.User;
import com.example.ott.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional
    public boolean reportReply(Reply reply, User reporter, String reason) {
        // 이미 해당 댓글을 신고한 경우 -> 중복 방지
        if (reportRepository.existsByCommentAndReporter(reply, reporter)) {
            return false;
        }

        // 새로운 신고 생성
        ReplyReport report = new ReplyReport();
        report.setreply(reply);
        report.setReporter(reporter);
        report.setReason(reason);

        // DB에 저장
        reportRepository.save(report);

        return true;
    }
}
