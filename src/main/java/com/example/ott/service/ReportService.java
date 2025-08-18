package com.example.ott.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ott.dto.ReportDTO;
import com.example.ott.entity.Reply;
import com.example.ott.entity.Report;
import com.example.ott.entity.User;
import com.example.ott.repository.ReplyRepository;
import com.example.ott.repository.ReportRepository;
import com.example.ott.repository.UserRepository;
import com.example.ott.type.Reason;
import com.example.ott.type.Status;

import lombok.RequiredArgsConstructor;

/**
 * 신고(Report) 관련 비즈니스 로직 처리 서비스 클래스
 */

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /** 전체 조회 */
    @Transactional(readOnly = true)
    public Page<Report> getReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ReportDTO> getReports(Collection<Reason> reasons,
            Collection<Status> statuses,
            Pageable pageable) {
        var R = sanitize(reasons);
        var S = sanitize(statuses);

        Page<Report> page;
        if (R == null && S == null)
            page = reportRepository.findAll(pageable);
        else if (R != null && S == null)
            page = reportRepository.findByReasonIn(R, pageable);
        else if (R == null && S != null)
            page = reportRepository.findByStatusIn(S, pageable);
        else
            page = reportRepository.findAllByFilters(R, S, pageable);

        return page.map(this::entityToDto); // 최종 변환
    }

    /** 컬렉션에서 null 제거 → 비면 null 반환 */
    private static <T> Collection<T> sanitize(Collection<T> input) {
        if (input == null)
            return null;
        Collection<T> filtered = input.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)); // 중복 제거 + 순서 유지
        return filtered.isEmpty() ? null : filtered;
    }

    /** 신고 상태 업데이트 (조회→수정) + 경고카운트 적용 */
    @Transactional
    public void updateReportStatus(ReportDTO dto) {
        Report report = reportRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 신고정보 입니다."));

        // 새 상태 보정
        Status newStatus = Status.orDefault(dto.getStatus());

        // 상태/사유 갱신
        report.setStatus(newStatus);
        if (dto.getReason() != null) {
            report.setReason(dto.getReason());
        }

        // ✅ 신고 엔티티를 통해 안전하게 대상 Reply 획득 (dto.replyId 필요 없음)
        Reply reply = report.getReply();
        if (reply == null) {
            throw new NoSuchElementException("신고 대상 댓글이 존재하지 않습니다.");
        }

        // 댓글에도 상태 반영(정책에 맞게)
        reply.setStatus(newStatus);

        // NO_ACTION(무혐의)면 신고 레코드는 삭제하고 종료
        if (newStatus == Status.NO_ACTION) {
            reportRepository.delete(report);
            return;
        }

        // 경고/삭제 등 처분에 따른 경고 카운트
        User target = reply.getReplyer();
        userService.addWarningCount(target.getId(), newStatus);
        // dirty checking으로 자동 반영
    }

    /** 신고 생성 */
    public Report createReport(ReportDTO dto) {
        Report report = dtoToEntity(dto);
        if (report.getStatus() == null) {
            Reply reply = replyRepository.findById(dto.getReplyId()).get();
            report.setStatus(Status.RECEIVED);
            reply.setStatus(Status.RECEIVED);
            replyRepository.save(reply);
        }

        return reportRepository.save(report);
    }

    /** 엔티티 → DTO (불필요한 추가 조회 제거) */
    @Transactional(readOnly = true)
    public ReportDTO entityToDto(Report report) {
        Reply reply = report.getReply();
        User reporter = report.getReporter();

        // reply가 null이어도 안전하게 값 추출
        Long replyId = Optional.ofNullable(reply)
                .map(Reply::getRno)
                .orElse(null); // 빈 값: null

        String replyNickName = Optional.ofNullable(reply)
                .map(Reply::getReplyer)
                .map(User::getNickname)
                .orElse(""); // 빈 값: 빈 문자열

        String text = Optional.ofNullable(reply)
                .map(Reply::getText)
                .orElse("");

        return ReportDTO.builder()
                .id(report.getId())
                .reporterId(reporter != null ? reporter.getId() : null)
                .replyId(replyId)
                .replyNickName(replyNickName)
                .reason(report.getReason())
                .reportDate(report.getCreatedDate())
                .handleDate(report.getUpdatedDate())
                .text(report.getText())
                .status(report.getStatus())
                .text(text)
                .build();
    }

    /** DTO → 엔티티 (생성/업데이트 공용, 업데이트 시 dto.id 필수) */
    public Report dtoToEntity(ReportDTO dto) {
        // 프록시로 참조만 잡아도 OK (불필요 쿼리 방지)
        Reply replyRef = replyRepository.getReferenceById(dto.getReplyId());
        User reporterRef = userRepository.getReferenceById(dto.getReporterId());

        return Report.builder()
                .id(dto.getId()) // 업데이트라면 필수
                .reporter(reporterRef)
                .reply(replyRef)
                .text(dto.getText())
                .reason(dto.getReason())
                .status(dto.getStatus())
                .build();
    }
}
