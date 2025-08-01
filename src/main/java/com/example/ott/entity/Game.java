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

@Entity
@ToString(exclude = { "image", "replies" })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Game extends BaseEntity {

    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String gid; // 게임코드 ex: g_1 / g_2

    @Column(nullable = false)
    private String appid; // steam 고유 코드
    @Column(nullable = false)
    private String title; // 게임명
    private String developer; // 개발사

    private int ccu; // 동시접속자 수

    private String platform; // 플랫폼

    private int rank; // 순위

    private String genres; // 장르

    private String originalPrice; // 할인 전 가격
    private String price; // 할인 적용된 현재 가격
    private int discountRate; // 할인율 (예: 20 -> 20%)
    private String publisher; // 배급사
    private String ageRating; // 이용연령등급

    private int positive; // 좋아요 수
    private int negative; // 싫어요 수
    @Column(length = 10000)
    private String synopsis; // 줄거리

    // @Builder.Default
    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>(); // 댓글

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id", nullable = true) // 외래 키는 image 테이블의 PK
    private Image image;

    @Builder.Default
    private int followcnt = 0; // 해당 컨텐츠에대한 팔로워 수

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    private Content content;
    // @Builder.Default
    // @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    // private List<Genre> genres = new ArrayList<>(); // 컨텐츠별 장르

    public void setFollowcnt(int followcnt) {
        this.followcnt = followcnt;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setOriginalPrice(String originalPrice) {
        this.originalPrice = originalPrice;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public void setPositive(int positive) {
        this.positive = positive;
    }

    public void setNegative(int negative) {
        this.negative = negative;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setCcu(int ccu) {
        this.ccu = ccu;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setImage(Image image) {
        this.image = image;

    }
}