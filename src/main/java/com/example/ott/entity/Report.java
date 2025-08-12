package com.example.ott.entity;

import java.time.LocalDateTime;

import com.example.ott.type.Reason;
import com.example.ott.type.Status;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "report")
public class Report extends BaseEntity {

    // 신고 고유 ID (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자: User 엔티티 참조 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 신고 대상 댓글: Reply 엔티티 참조 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = false)
    private Reply reply;

    // 신고 사유 (enum 클래스)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 200)
    private Reason reason;

    // 처리 상태 (예: RECEIVED, WARNING, DELETED, NO_ACTION)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    // 알림 확인여부
    private boolean checked;

    // 처리자 이름 (관리자명)
    // @Column(length = 100)
    // private String handlerName;

}
