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

    private String gid;

    @Column(nullable = false)
    private String appid;
    @Column(nullable = false)
    private String title;
    private String developer;

    private int ccu;

    private String platform;

    private int rank;

    private String genres;

    private String originalPrice; // 할인 전 가격
    private String price; // 할인 적용된 현재 가격
    private int discountRate; // 할인율 (예: 20 -> 20%)
    private String publisher; // 배급사
    private String ageRating; // 이용연령등급

    private int positive;
    private int negative;
    @Column(length = 10000)
    private String synopsis;

    @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id", nullable = true)
    private Image image;

    @Builder.Default
    private int followcnt = 0; // 해당 컨텐츠에대한 팔로워 수

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