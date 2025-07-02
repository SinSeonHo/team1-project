package com.example.ott.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.ott.entity.Image;
import com.example.ott.entity.Reply;

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
    private String genres; // 장르
    private int showTm; // 상영시간
    private String nationNm; // 제작국가
    private String gradeNm; // 이용등급
    private String synopsis; // 줄거리

    private int followcnt;
    private String imgUrl; // 이미지경로
    private int replycnt; // 댓글갯수

    // @Builder.Default
    // private List<Image> movieGenres = new ArrayList<>();

    // @Builder.Default
    // private List<Image> movieReplies = new ArrayList<>();

}