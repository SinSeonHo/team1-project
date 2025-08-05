package com.example.ott.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter

@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(value = AuditingEntityListener.class)
@Builder
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rno;

    @Column(nullable = false)
    @Size(max = 200)
    private String text;

    @Builder.Default
    // 리뷰의 평점
    private int recommend = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private User replyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mid")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gid")
    private Game game;

    // 대상 댓글의 rno
    @Setter
    private Long ref;

    // 대상 댓글의 유저 아이디
    @Setter
    private String mention;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    public void changeText(String text) {
        this.text = text;
    }

    public void changeRate(int recommend) {
        this.recommend = recommend;
    }
}
