package com.example.ott.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno; // 댓글 PK

    @Column(nullable = false)
    @Size(max = 200)
    private String text; // 댓글 내용

    @Builder.Default
    // 추천 수 (평점)
    private int recommend = 0;

    // 댓글 작성자 (User) - ManyToOne, 외래키 replyer_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replyer_id")
    private User replyer;

    // 댓글이 작성된 영화 (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mid")
    private Movie movie;

    // 댓글이 작성된 게임 (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gid")
    private Game game;

    // 대상 댓글 번호(답글인 경우) - nullable 허용
    @Setter
    @Column(nullable = true)
    private Long ref;

    // 대상 댓글 작성자 아이디 (멘션된 경우) - nullable 허용
    @Setter
    @Column(nullable = true)
    private String mention;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate; // 댓글 작성 시간

    @LastModifiedDate
    private LocalDateTime updatedDate; // 댓글 수정 시간

    // 댓글 내용 수정 메서드
    public void changeText(String text) {
        this.text = text;
    }

    // 추천 수 변경 메서드
    public void changeRate(int recommend) {
        this.recommend = recommend;
    }
}
