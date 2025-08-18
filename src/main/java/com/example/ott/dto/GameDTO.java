package com.example.ott.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GameDTO {

    private String gid;
    private String appid;
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
    private String synopsis;

    private int followcnt;
    private String imgUrl;
    private int replycnt;
    private List<ReplyDTO> Replies; // 댓글
}