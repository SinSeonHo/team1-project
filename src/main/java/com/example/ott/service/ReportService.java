package com.example.ott.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import lombok.extern.log4j.Log4j2;

/**
 * 신고(Report) 관련 비즈니스 로직 처리 서비스 클래스
 */
@Log4j2
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
    public void updateReportStatus(ReportDTO dto) {
        Report report = reportRepository.findById(dto.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 신고정보 입니다."));

        // 상태/사유 갱신
        report.setStatus(dto.getStatus());
        // report.setReason(dto.getReason());
        report.setChecked(true); // 필요 시

        // 신고 대상 유저는 엔티티 경로로 신뢰성 있게 획득
        User target = report.getReply().getReplyer();

        // 처리 결과에 따른 경고 카운트
        userService.addWarningCount(target.getId(), dto.getStatus());
        // save() 불필요 — dirty checking
    }

    /** 신고 생성 */
    public Report createReport(ReportDTO dto) {
        Report report = dtoToEntity(dto);
        if (report.getStatus() == null) {
            report.setStatus(Status.RECEIVED);
        }
        log.info("report 정보 : {}", report);
        return reportRepository.save(report);
    }

    /** 엔티티 → DTO (불필요한 추가 조회 제거) */
    @Transactional(readOnly = true)
    public ReportDTO entityToDto(Report report) {
        Reply reply = report.getReply();
        User reporter = report.getReporter();
        User reportTarget = reply.getReplyer();

        ReportDTO reportDTO = ReportDTO.builder()
                .id(report.getId())
                .reporterId(reporter.getId())
                .replyId(reply.getRno())
                .replyNickName(reportTarget.getNickname())
                .reason(report.getReason())
                .reportDate(report.getCreatedDate())
                .handleDate(report.getUpdatedDate())
                .status(report.getStatus())
                .build();

        return reportDTO;

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
                .reason(dto.getReason())
                .status(dto.getStatus())
                .build();
    }
}
