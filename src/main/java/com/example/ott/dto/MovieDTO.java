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
public class MovieDTO {

    private String mid; // 영화코드
    private String movieCd; // KOBIS 고유 코드
    private String title; // 영화명
    private String actors; // 배우들
    private String director; // 감독
    private String openDate; // 개봉일
    private int rank; // 순위
    private String genres;

    // 평점
    private double avg;

    // 리뷰수
    private Long reviewCnt;

    // @Builder.Default
    // private List<Image> movieImages = new ArrayList<>();

    // @Builder.Default
    // private List<Image> movieGenres = new ArrayList<>();

    // @Builder.Default
    // private List<Image> movieReplies = new ArrayList<>();

}
