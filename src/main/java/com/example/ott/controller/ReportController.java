package com.example.ott.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ott.entity.Reply;
import com.example.ott.entity.User;
import com.example.ott.service.ReplyService;
import com.example.ott.service.ReportService;
import com.example.ott.service.UserService;

@Controller
public class ReportController {
    private final ReplyService replyService;
    private final UserService userService;
    private final ReportService reportService;

    public CommentReportController(ReplyService replyService,
                                   UserService userService,
                                   ReportService reportService) {
        this.replyService = replyService;
        this.userService = userService;
        this.reportService = reportService;
    }

    // 댓글 신고 요청 처리
    @PostMapping("/comments/{id}/report")
    public String reportComment(@PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        // 현재 로그인한 사용자 가져오기
        User reporter = userService.findByUsername(userDetails.getUsername());

        // 댓글 ID로 댓글 가져오기
        Reply reply = replyService.findById(id);

        // 신고 시도
        boolean success = reportService.reportComment(reply, reporter, reason);

        if (success) {
            redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "이미 신고한 댓글입니다.");
        }

        // 원래 페이지로 리디렉션 (댓글 위치는 필요 시 수정)
        return "redirect:/some-page";
    }
}
