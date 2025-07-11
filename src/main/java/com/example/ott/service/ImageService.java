package com.example.ott.service;

import com.example.ott.entity.Image;

import com.example.ott.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir;

    private final String thumbnailDirName = "thumbnails";
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB 제한

    // 원본 + 썸네일 저장 및 DB 저장
    public Image uploadImageWithThumbnail(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "." + extension;

        Path saveFolder = Paths.get(baseDir);
        Files.createDirectories(saveFolder);

        Path originalPath = saveFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

        // 썸네일 생성
        String thumbnailName = uuid + "_thumb." + extension;
        Path thumbnailFolder = saveFolder.resolve(thumbnailDirName);
        Files.createDirectories(thumbnailFolder);

        Path thumbnailPath = thumbnailFolder.resolve(thumbnailName);
        Thumbnails.of(originalPath.toFile())
                .size(200, 200)
                .toFile(thumbnailPath.toFile());

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(savedFileName)
                .thumbnailPath(thumbnailDirName + "/" + thumbnailName)
                .build();

        return imageRepository.save(image);
    }

    // 썸네일 전용 업로드 (원본 저장 + 썸네일 생성 및 저장, DB 저장)
    public Image uploadThumbnailImage(MultipartFile file) throws IOException {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();

        Path saveFolder = Paths.get(baseDir);
        Files.createDirectories(saveFolder);

        // 원본 파일 저장
        String savedFileName = uuid + "." + extension;
        Path originalPath = saveFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

        // 썸네일 생성
        String thumbnailName = uuid + "_thumb." + extension;
        Path thumbnailFolder = saveFolder.resolve(thumbnailDirName);
        Files.createDirectories(thumbnailFolder);

        Path thumbnailPath = thumbnailFolder.resolve(thumbnailName);
        Thumbnails.of(originalPath.toFile())
                .size(200, 200)
                .toFile(thumbnailPath.toFile());

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(savedFileName)
                .thumbnailPath(thumbnailDirName + "/" + thumbnailName)
                .build();

        return imageRepository.save(image);
    }

    // 원본 이미지 조회
    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        return getFileAsResource(filePath);
    }

    // 썸네일 이미지 조회
    public Resource getThumbnailFile(String filename) {
        Path filePath = Paths.get(baseDir, thumbnailDirName).resolve(filename);
        return getFileAsResource(filePath);
    }

    // 공통 파일 조회 처리
    private Resource getFileAsResource(Path path) {
        if (!Files.exists(path) || !Files.isReadable(path)) {
            log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", path);
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(path);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("파일이 유효하지 않습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 30MB를 초과할 수 없습니다.");
        }
    }

    private String getFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx != -1 && idx < filename.length() - 1) ? filename.substring(idx + 1).toLowerCase() : "jpg";
    }
}