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

@Entity
@ToString(exclude = { "image", "replies" })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "game")
public class Game extends BaseEntity {

    @Id
    @Column(length = 50) // gid 최대 길이 지정 (필요시 조정)
    private String gid;

    @Column(nullable = false)
    private String appid;

    @Column(nullable = false)
    private String title;

    private String developer;

    private int ccu;

    private String platform;

    private int ranking;

    private String genres;

    private String originalPrice; // 할인 전 가격

    private String price; // 할인 적용된 현재 가격

    private int discountRate; // 할인율 (예: 20 -> 20%)

    private String publisher; // 배급사

    private String ageRating; // 이용연령등급

    private int positive;

    private int negative;

    @Lob
    @Column(columnDefinition = "TEXT") // MySQL TEXT 타입으로 큰 텍스트 저장
    private String synopsis;

    @Builder.Default
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id") // nullable = true 는 기본값이라 생략
    private Image image;

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

    public void setRanking(int ranking) {
        this.ranking = ranking;
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