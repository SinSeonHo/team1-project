package com.example.ott.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder

public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid; // 영화코드

    private String title; // 영화명
    private String actors; // 등장 배우들 하나의 칼럼으로 나열만 할 예정
    private String director; // 감독
    private String story; // 줄거리


    private String genres;
    // @Builder.Default
    // @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    // private List<Image> img; // 이미지 리스트로 관리필요 추후 이미지 작성 후 연동예정

}
