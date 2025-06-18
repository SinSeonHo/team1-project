package com.example.ott.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "movie", "member", "webtoon", "game" })
@Getter
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long id;

    @Column(nullable = false)
    private int rating;

    // @JoinColumn(name = "novel_id")
    // @ManyToOne(fetch = FetchType.LAZY)
    // private Novel novel;

    // @JoinColumn(name = "member_id")
    // @ManyToOne(fetch = FetchType.LAZY)
    // private Member member;
}
