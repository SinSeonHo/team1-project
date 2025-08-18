package com.example.ott.entity;

import com.example.ott.type.Reason;
import com.example.ott.type.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@ToString(exclude = { "reporter", "reply" })
@Table(name = "report")
public class Report extends BaseEntity {

    // 신고 고유 ID (자동 생성)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자: User 엔티티 참조 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = true)
    private User reporter;

    // 신고 대상 댓글: Reply 엔티티 참조 (ManyToOne)
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id", nullable = true) // ← null 허용 OK
    private Reply reply;

    // 신고 사유 (enum 클래스)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Reason reason;

    // 처리 상태 (예: RECEIVED, WARNING, DELETED, NO_ACTION)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.RECEIVED;

    private String text;

}
