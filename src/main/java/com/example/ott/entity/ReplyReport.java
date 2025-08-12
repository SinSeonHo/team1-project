package com.example.ott.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ReplyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성되는 ID
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 신고된 댓글
    @JoinColumn(name = "comment_id")
    private Reply reply;

    @ManyToOne(fetch = FetchType.LAZY) // 신고한 사용자
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private String reason; // 신고 사유

    private LocalDateTime reportedAt = LocalDateTime.now(); // 신고 시간

    // Getter/Setter 생략 가능 (필요시 Lombok 사용)
}
