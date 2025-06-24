package com.example.ott.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ott.entity.Image;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.junit.jupiter.api.Test;

@SpringBootTest
public class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Test
    public void insertImageTest() {
        // 가정: 파일은 이미 /uploads/2025-06-11/ 디렉토리에 저장돼 있음
        String originalFileName = "no image.png";
        String uuid = UUID.randomUUID().toString(); // 임의 UUID 생성
        String extension = ".png";
        String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String savedFileName = uuid + extension;

        // 실제 경로: /uploads/2025-06-11/uuid.png
        String relativePath = dateFolder + "/" + savedFileName;

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(relativePath)
                .build();

        imageRepository.save(image);
        System.out.println("이미지 저장 완료: " + image.getInum());
    }

}
