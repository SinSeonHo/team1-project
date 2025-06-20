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
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir;

    /**
     * 파일을 업로드하고 DB에 저장
     * 
     * @param file MultipartFile
     * @return 저장된 Image
     * @throws IOException 파일 저장 실패 시
     */

    public Image uploadImages(MultipartFile file) throws IOException {
        // 1. 파일명 확인
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        // 2. 확장자 추출
        int idx = originalFileName.lastIndexOf('.');
        String extension = (idx != -1 && idx < originalFileName.length() - 1) ? originalFileName.substring(idx) : "";

        // 3. 저장할 파일명 UUID로 생성
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + extension;

        // 4. 날짜별 폴더 경로
        String dateFolder = LocalDate.now().toString(); // 예: 2025-06-11
        Path saveFolder = Paths.get(baseDir, dateFolder);
        Files.createDirectories(saveFolder); // 폴더 없으면 생성

        // 5. 파일 저장
        Path targetPath = saveFolder
                .resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 6. DB 저장용 상대 경로
        String relativePath = dateFolder + "/" + savedFileName;

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(relativePath)
                .build();

        return imageRepository.save(image);
    }

    /**
     * 파일을 Resource 형태로 반환 (이미지 뷰용)
     * 
     * @param filename 저장된 파일명 (예: 2025-06-11/uuid.jpg)
     * @return Resource
     **/
    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }
}
