package com.example.ott.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.ott.entity.Image;
import com.example.ott.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir;

    private final String thumbnailDirName = "thumbnails";

    public Image findById(Long inum) {
        return imageRepository.findById(inum).orElse(null);
    }

    // 원본 이미지 업로드 (썸네일 없이)
    public Image uploadOriginalImage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateFileName(originalFileName);

        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "." + extension;

        Path saveFolder = Paths.get(baseDir);
        Files.createDirectories(saveFolder);

        Path targetPath = saveFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(savedFileName)
                .thumbnailPath(null)
                .build();

        return imageRepository.save(image);
    }

    // 썸네일 이미지 MultipartFile로 업로드 (썸네일 전용 폴더에 저장)
    public Image uploadThumbnailImage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateFileName(originalFileName);

        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "." + extension;

        Path thumbnailFolder = Paths.get(baseDir, thumbnailDirName);
        Files.createDirectories(thumbnailFolder);

        Path targetPath = thumbnailFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // URL 경로에서 슬래시 사용 권장
        String thumbnailPath = thumbnailDirName + "/" + savedFileName;

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(null) // 원본 경로가 없으므로 null 처리
                .thumbnailPath(thumbnailPath)
                .build();

        return imageRepository.save(image);
    }

    // 저장된 이미지로 썸네일 생성 및 DB 업데이트 (썸네일 전용 폴더에 저장)
    public void createThumbnail(Image image) throws IOException {
        if (image.getPath() == null || image.getPath().isBlank()) {
            throw new IllegalArgumentException("원본 이미지 경로가 없습니다.");
        }

        Path originalFile = Paths.get(baseDir).resolve(image.getPath());
        if (!Files.exists(originalFile)) {
            throw new IOException("원본 파일이 존재하지 않습니다: " + originalFile.toString());
        }

        String extension = getFileExtension(image.getPath());
        String thumbnailName = image.getUuid() + "_thumb." + extension;

        Path thumbnailFolder = Paths.get(baseDir, thumbnailDirName);
        Files.createDirectories(thumbnailFolder);

        Path thumbnailFile = thumbnailFolder.resolve(thumbnailName);

        Thumbnails.of(originalFile.toFile())
                .size(200, 200)
                .toFile(thumbnailFile.toFile());

        // URL 경로 슬래시 사용
        String thumbnailPath = thumbnailDirName + "/" + thumbnailName;
        image.setThumbnailPath(thumbnailPath);
        imageRepository.save(image);

        log.info("썸네일 생성 완료: {}", thumbnailFile.toString());
    }

    // 원본 이미지 파일 제공
    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }

    // 썸네일 파일 제공 (썸네일 폴더 경로 포함)
    public Resource getThumbnailFile(String filename) {
        Path filePath = Paths.get(baseDir, thumbnailDirName).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("썸네일 파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("썸네일 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }

    // 확장자 추출 유틸
    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank())
            return "jpg";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1 && dotIndex < filename.length() - 1)
                ? filename.substring(dotIndex + 1).toLowerCase()
                : "jpg";
    }

    // 파일명 유효성 체크
    private void validateFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }
    }
}
