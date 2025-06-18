package com.example.ott.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gid; // 게임코드

    private String title; // 게임명
    private String developer; // 개발사
    @Enumerated(EnumType.STRING)
    
    private String platform; // 플랫폼
    private int price; // 가격


    private String genres;
    // @Builder.Default
    // @OneToMany(mappedBy = "game", cascade = CascadeType.PERSIST)
    // private List<Image> img; // 이미지 리스트로 관리필요 추후 이미지 작성 후 연동예정

}
