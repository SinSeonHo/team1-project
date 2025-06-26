package com.example.ott.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.ott.entity.Image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameDTO {

    private String gid; // 게임코드
    private String appid; // steam 고유 코드
    private String title; // 게임명
    private String developer; // 개발사
    private int ccu; // 동시접속자 수
    private String platform; // 플랫폼

    private int rank; // 순위
    private String genres; // 장르

    private int originalPrice; // 할인 전 가격
    private int price; // 할인 적용된 현재 가격
    private int discountRate; // 할인율 (예: 20 -> 20%)
    private String publisher; // 배급사
    private String ageRating; // 이용연령등급

    private int positive; // 좋아요 수
    private int negative; // 싫어요 수
    private String synopsis; // 줄거리

    private String imgUrl; // 이미지경로
    private int replycnt; // 댓글갯수

    // @Builder.Default
    // private List<Image> gameImages = new ArrayList<>();

    // @Builder.Default
    // private List<Image> gameGenres = new ArrayList<>();

    // @Builder.Default
    // private List<Image> gameReplies = new ArrayList<>();

}