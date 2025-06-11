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
    private int price; // 가격
    private int rank; // 순위

    // @Builder.Default
    // private List<Image> gameImages = new ArrayList<>();

    // @Builder.Default
    // private List<Image> gameGenres = new ArrayList<>();

    // @Builder.Default
    // private List<Image> gameReplies = new ArrayList<>();

}
