package com.example.ott.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String title; // 영화명

    private String actors; // 배우들

    private String director; // 감독

    // @Builder.Default
    // private List<MovieImageDTO> movieImages = new ArrayList<>();

    private String openDate;

    private int rank;

    private String movieCd; // KOBIS 고유 코드

    // 평점
    private double avg;

    // 리뷰수
    private Long reviewCnt;

}
