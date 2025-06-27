package com.example.ott.dto;

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

    private int rank;
    private String genres;

    private int originalPrice;
    private int price;
    private int discountRate;
    private String publisher;
    private String ageRating;

    private int positive;
    private int negative;
    private String synopsis;

    private int followcnt;
    private String imgUrl;
    private int replycnt;

}