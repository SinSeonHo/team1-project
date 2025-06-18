package com.example.ott.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(exclude = { "images", "replies" })
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
    // private String actors; // 등장 배우들 하나의 칼럼으로 나열만 할 예정
    // private String director; // 감독


    private String openDate;

    @Column(nullable = false)
    private int rank;

    @Column(unique = true)
    private String movieCd; // KOBIS 고유 코드

    private String director; // 감독 이름
    @Column(length = 1000)
    private String actors; // 배우 이름들을 쉼표로 나열한 문자열

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>(); // 댓글

    @Builder.Default
    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Image> images = new ArrayList<>(); // 이미지 리스트로 관리필요 추후 이미지 작성 후 연동예정

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Genre> genres = new ArrayList<>();

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
}
