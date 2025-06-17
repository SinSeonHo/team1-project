package com.example.ott.service;

import com.example.ott.entity.Image;
import com.example.ott.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir; // 예: application.properties 에서 설정

    public Image uploadImage(MultipartFile file, int ord) throws IOException {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + extension;

        Path saveDir = Paths.get(baseDir);
        Files.createDirectories(saveDir);

        Path targetPath = saveDir.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(savedFileName) // 저장된 경로 (상대 경로 or 절대경로 일부)
                .ord(ord)
                .build();

        return imageRepository.save(image);
    }

    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        return new FileSystemResource(filePath);
    }
}
