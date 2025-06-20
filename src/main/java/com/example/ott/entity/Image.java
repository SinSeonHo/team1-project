package com.example.ott.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "movie", "game", "user" }) // 순환참조 방지
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inum; // 이미지 번호
    private String uuid; // 이미지 고유번호
    private String imgName; // 이미지 이름
    private String path; // 경로

    @OneToOne(mappedBy = "image")
    private Movie movie;

    @OneToOne(mappedBy = "image")
    private Game game;

    @OneToOne(mappedBy = "image")
    private User user;

}