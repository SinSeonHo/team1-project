package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
public class Movie extends BaseEntity {

    @Id

    private String mid;

    @Column(nullable = false)
    private String title;
    private String openDate;

    @Column(nullable = false)
    private int ranking;

    @Column(unique = true)
    private String movieCd;

    private String director;
    @Column(length = 1000)
    private String actors;

    private String genres; // 장르
    private String showTm; // 상영시간
    private String nationNm; // 제작국가
    private String gradeNm; // 이용등급
    @Column(length = 10000)
    private String synopsis;

    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id", nullable = true)
    private Image image;

}