package com.example.ott.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDTO {
    private Long id; // 신고 고유 ID
    private String reporterId; // 신고자 아이디

    private Long replyId; // 신고 대상 댓글 ID
    private String replyNickName; // 신고 대상 닉네임
    private String reason; // 신고 사유

    private LocalDateTime reportDate; // 신고 일시
    private LocalDateTime handleDate; // 처리 일시
    private String status; // 처리 상태

}
