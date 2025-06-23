package com.example.ott.service;

import com.example.ott.entity.Image;
import com.example.ott.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir;

    // 1. 이미지 업로드 및 DB 저장
    public Image uploadImages(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        // 확장자 추출
        int idx = originalFileName.lastIndexOf('.');
        String extension = (idx != -1 && idx < originalFileName.length() - 1) ? originalFileName.substring(idx) : "";

        // 저장할 UUID 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + extension;

        // 저장할 경로 (고정된 baseDir)
        Path saveFolder = Paths.get(baseDir);
        Files.createDirectories(saveFolder); // 폴더 없으면 생성

        Path targetPath = saveFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 상대 경로는 단순 파일명만 저장 (날짜 제거)
        String relativePath = savedFileName;

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(relativePath) // 단순 파일명만
                .build();

        return imageRepository.save(image);
    }

    // 2. 이미지 불러오기
    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }
}
