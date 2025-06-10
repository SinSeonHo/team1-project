package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString(exclude = { "images", "replies" })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Game extends BaseEntity {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gid; // 게임코드 ex: g_1 / g_2

    @Column(nullable = false)
    private String title; // 게임명
    private String developer; // 개발사
    private String platform; // 플랫폼
    private int price; // 가격
    private int rank; // 순위

    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>(); // 댓글

    @Builder.Default
    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    private List<Image> images = new ArrayList<>(); // 이미지 리스트로 관리필요 추후 이미지 작성 후 연동예정

    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    private List<Genre> genres = new ArrayList<>(); // 컨텐츠별 장르

    public void setPrice(int price) {
        this.price = price;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
