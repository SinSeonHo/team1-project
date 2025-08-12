// package com.example.ott.service;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.ott.entity.Report;
// import com.example.ott.dto.ReportDTO;
// import com.example.ott.entity.Reply;
// import com.example.ott.entity.User;
// import com.example.ott.repository.ReportRepository;
// import com.example.ott.type.Reason;

// import lombok.RequiredArgsConstructor;

// /**
// * 신고(Report) 관련 비즈니스 로직 처리 서비스 클래스
// */
// @Service
// @RequiredArgsConstructor
// @Transactional
// public class ReportService {

// private final ReportRepository reportRepository;

// /**
// * 전체 신고 목록 조회 (페이징 없이 전체 조회)
// *
// * @return 전체 신고 리스트
// */
// @Transactional(readOnly = true)
// public List<Report> getAllReports() {
// return reportRepository.findAll();
// }

// /**
// * 신고 등록 처리
// *
// * @param reporter 신고자 User 엔티티
// * @param reply 신고 대상 Reply 엔티티
// * @param reason 신고 사유
// * @param detail 상세 설명 (선택적)
// * @param evidenceUrl 증거 URL (선택적)
// * @return 저장된 Report 엔티티
// */
// public Report createReport(ReportDTO dto) {
// Report report = Report.builder()
// .reporter(reporter)
// .reply(reply)
// .reason(Reason.SPOILER)
// // .detail(detail)
// // .evidenceUrl(evidenceUrl)
// // .reportDate(LocalDateTime.now())
// .status("PENDING") // 기본 상태는 '대기'
// .build();
// return reportRepository.save(report);
// }

// /**
// * 신고 ID로 단건 조회
// *
// * @param id 신고 ID
// * @return Optional로 감싼 Report 엔티티
// */
// @Transactional(readOnly = true)
// public Optional<Report> getReportById(Long id) {
// return reportRepository.findById(id);
// }

// /**
// * 특정 신고자별 신고 목록 조회
// *
// * @param reporter 신고자 User 엔티티
// * @return 신고 리스트
// */
// @Transactional(readOnly = true)
// public List<Report> getReportsByReporter(User reporter) {
// return reportRepository.findByReporter(reporter);
// }

// /**
// * 특정 댓글별 신고 목록 조회
// *
// * @param reply 신고 대상 Reply 엔티티
// * @return 신고 리스트
// */
// @Transactional(readOnly = true)
// public List<Report> getReportsByReply(Reply reply) {
// return reportRepository.findByReply(reply);
// }

// /**
// * 상태별 신고 목록 조회
// *
// * @param status 상태 문자열 (예: "PENDING", "DONE")
// * @return 신고 리스트
// */
// @Transactional(readOnly = true)
// public List<Report> getReportsByStatus(String status) {
// return reportRepository.findByStatus(status);
// }

// /**
// * 신고 상태 및 처리자 정보 업데이트
// *
// * @param reportId 신고 ID
// * @param newStatus 새로운 상태 (예: PENDING, IN_PROGRESS, DONE, REJECTED)
// * @param handlerName 처리자 이름 (관리자명)
// */
// public void updateReportStatus(Long reportId, String newStatus, String
// handlerName) {
// Optional<Report> optReport = reportRepository.findById(reportId);
// if (optReport.isPresent()) {
// Report report = optReport.get();
// report.setStatus(newStatus);
// report.setHandlerName(handlerName);
// report.setHandleDate(LocalDateTime.now());
// reportRepository.save(report);
// } else {
// throw new IllegalArgumentException("신고 내역을 찾을 수 없습니다. ID: " + reportId);
// }
// }

// /**
// * 페이징 및 검색(상태 + 키워드) 조회
// *
// * @param status 상태 필터 (null 또는 빈 문자열이면 상태 무시)
// * @param keyword 검색어 (신고자 ID, 닉네임, 사유 등 포함)
// * @param pageable 페이징 정보
// * @return 페이징된 신고 검색 결과
// */
// @Transactional(readOnly = true)
// public Page<Report> searchReports(String status, String keyword, Pageable
// pageable) {
// boolean noStatus = (status == null || status.isEmpty());
// boolean noKeyword = (keyword == null || keyword.isEmpty());

// if (noStatus && noKeyword) {
// // 상태, 키워드 모두 없음 -> 전체 조회 페이징
// return reportRepository.findAll(pageable);
// } else if (noStatus) {
// // 상태 없음, 키워드만 있음
// return reportRepository.findByKeyword(keyword, pageable);
// } else if (noKeyword) {
// // 키워드 없음, 상태만 있음
// return reportRepository.findByStatus(status, pageable);
// } else {
// // 상태 + 키워드 모두 있음
// return reportRepository.findByStatusAndKeyword(status, keyword, pageable);
// }
// }
// }
