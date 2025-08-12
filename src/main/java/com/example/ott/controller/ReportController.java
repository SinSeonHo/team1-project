// package com.example.ott.controller;

// import java.security.Principal;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import com.example.ott.entity.Report;
// import com.example.ott.entity.Reply;
// import com.example.ott.entity.User;
// import com.example.ott.service.ReportService;
// import com.example.ott.service.ReplyService;
// import com.example.ott.service.UserService;

// import lombok.RequiredArgsConstructor;

// /**
// * 신고(Report) 관련 요청 처리 컨트롤러
// * - 관리자용 신고 리스트 페이지 렌더링
// * - 신고 REST API (신고 등록, 상태 변경 등)
// */
// @Controller
// @RequestMapping("/admin/report")
// @RequiredArgsConstructor
// public class ReportController {

// private final ReportService reportService;
// private final UserService userService;
// private final ReplyService replyService;

// /**
// * 관리자용 신고 리스트 페이지 (복수형 경로 권장)
// *
// * @param status 필터 상태 (optional)
// * @param model 뷰에 전달할 모델
// * @return 신고 리스트 페이지 뷰 이름
// */
// @GetMapping("/list")
// public String showReportList(@RequestParam(required = false) String status,
// Model model) {
// List<Report> reports;

// if (status == null || status.isEmpty()) {
// reports = reportService.getAllReports();
// } else {
// reports = reportService.getReportsByStatus(status);
// }

// model.addAttribute("reports", reports);
// model.addAttribute("selectedStatus", status);

// return "report/reportlist"; // templates/admin/report.html 렌더링
// }

// /**
// * 신고 등록 API
// */
// @PostMapping("/create")
// @ResponseBody
// public ResponseEntity<String> createReport(Principal principal,
// @RequestParam Long replyId,
// @RequestParam String reason,
// @RequestParam(required = false) String detail,
// @RequestParam(required = false) String evidenceUrl) {

// String userId = principal.getName();

// Optional<User> optionalUser = userService.findById(userId);
// if (optionalUser.isEmpty()) {
// return ResponseEntity.badRequest().body("신고자 정보가 없습니다.");
// }
// User reporter = optionalUser.get();

// Optional<Reply> optionalReply = replyService.findById(replyId);
// if (optionalReply.isEmpty()) {
// return ResponseEntity.badRequest().body("신고 대상 댓글이 존재하지 않습니다.");
// }
// Reply reply = optionalReply.get();

// reportService.createReport(reporter, reply, reason, detail, evidenceUrl);

// return ResponseEntity.ok("신고가 접수되었습니다.");
// }

// /**
// * 특정 상태에 해당하는 신고 목록 조회 API (기본 상태: PENDING)
// */
// @GetMapping
// @ResponseBody
// public ResponseEntity<List<Report>> getAllReports() {
// List<Report> reports = reportService.getReportsByStatus("PENDING");
// return ResponseEntity.ok(reports);
// }

// /**
// * 신고 상태 변경 API (관리자 전용)
// */
// @PostMapping("/{id}/status")
// @ResponseBody
// public ResponseEntity<String> updateStatus(@PathVariable Long id,
// @RequestParam String status,
// Principal principal) {

// String handlerName = principal.getName();

// try {
// reportService.updateReportStatus(id, status, handlerName);
// return ResponseEntity.ok("신고 상태가 업데이트 되었습니다.");
// } catch (IllegalArgumentException e) {
// return ResponseEntity.badRequest().body(e.getMessage());
// }
// }
// }
