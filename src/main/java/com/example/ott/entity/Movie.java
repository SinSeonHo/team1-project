package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "replies", "image" })
@Getter
@Setter
@Builder
@Entity
@Table(name = "movie")
public class Movie extends BaseEntity {

    @Id
    @Column(length = 50) // mid 최대 길이 지정 (필요시 조정)
    private String mid;

    @Column(nullable = false)
    private String title;

    private String openDate;

    @Column(nullable = false)
    private int ranking;

    @Column(unique = true, length = 50)
    private String movieCd;

    private String director;

    @Column(length = 1000)
    private String actors;

    private String genres; // 장르

    private String showTm; // 상영시간

    private String nationNm; // 제작국가

    private String gradeNm; // 이용등급

    @Lob
    @Column(columnDefinition = "TEXT") // MySQL TEXT 타입으로 큰 텍스트 저장
    private String synopsis;

    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id") // nullable=true는 기본값이라 생략
    private Image image;

}
