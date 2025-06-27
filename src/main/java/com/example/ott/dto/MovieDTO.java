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
public class MovieDTO {

    private String mid;
    private String movieCd;
    private String title;
    private String actors;
    private String director;
    private String openDate;
    private int rank;
    private String genres;
    private int showTm;
    private String nationNm;
    private String gradeNm;
    private String synopsis;

    private int followcnt;
    private String imgUrl;
    private int replycnt;

}