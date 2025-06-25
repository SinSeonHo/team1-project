package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = { "image", "replies" })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class Movie extends BaseEntity {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String mid; // 영화코드 ex: m_1 / m_2

    @Column(nullable = false)
    private String title; // 영화명
    private String openDate; // 개봉일

    @Column(nullable = false)
    private int rank; // 순위

    @Column(unique = true)
    private String movieCd; // KOBIS 고유 코드

    private String director; // 감독 이름
    @Column(length = 1000)
    private String actors; // 배우 이름들을 쉼표로 나열한 문자열

    private String genres; // 장르
    private int showTm; // 상영시간
    private String nationNm; // 제작국가
    private String gradeNm; // 이용등급
    @Column(length = 10000)
    private String synopsis; // 줄거리

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>(); // 댓글

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id", nullable = true) // 외래 키는 image 테이블의 PK
    private Image image;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}