package com.example.ott.entity;

import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inum; // 이미지 번호

    private String uuid; // 이미지 고유 UUID
    private String imgName; // 원본 이미지 이름
    private String path; // 원본 이미지 저장 경로
    private String thumbnailPath; // 썸네일 이미지 저장 경로

    // screenshots 리스트를 별도 테이블에 저장
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "image_screenshots", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "screenshot_url", length = 1000)
    private List<String> screenshots;

    @OneToOne(mappedBy = "image")
    private Movie movie;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private Game game;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private User user;
}
