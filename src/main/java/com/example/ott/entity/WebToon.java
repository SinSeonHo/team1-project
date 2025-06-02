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

public class WebToon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wid; // 웹툰코드

    private String title; // 웹툰명
    private String author; // 작가명
    private String story; // 줄거리

    // @Builder.Default
    // @OneToMany(mappedBy = "webtoon", cascade = CascadeType.PERSIST)
    // private List<Image> img; // 이미지 리스트로 관리필요 추후 이미지 작성 후 연동예정

}
