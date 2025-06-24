package com.example.ott.service;

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
// 현재는 작동 불가 이미지 파일 이름에 ../ 같은 경로 탈출이 포함되지 않도록 검증 로직넣어둔 실험파일
public class ImageServiceCopy {

    private final ImageRepository imageRepository;

    @Value("${upload.base-dir}")
    private String baseDir;

    private final String thumbnailDirName = "thumbnails";
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB

    public Image findById(Long inum) {
        return imageRepository.findById(inum).orElse(null);
    }

    public Image uploadOriginalImage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateFileName(originalFileName);
        validateFileSize(file);

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

    public Image uploadThumbnailImage(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        validateFileName(originalFileName);
        validateFileSize(file);

        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + "." + extension;

        Path thumbnailFolder = Paths.get(baseDir, thumbnailDirName);
        Files.createDirectories(thumbnailFolder);

        Path targetPath = thumbnailFolder.resolve(savedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String thumbnailPath = thumbnailDirName + "/" + savedFileName;

        Image image = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(null)
                .thumbnailPath(thumbnailPath)
                .build();

        return imageRepository.save(image);
    }

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

        String thumbnailPath = thumbnailDirName + "/" + thumbnailName;
        image.setThumbnailPath(thumbnailPath);
        imageRepository.save(image);

        log.info("썸네일 생성 완료: {}", thumbnailFile.toString());
    }

    public Resource getImageFile(String filename) {
        Path filePath = Paths.get(baseDir).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }

    public Resource getThumbnailFile(String filename) {
        Path filePath = Paths.get(baseDir, thumbnailDirName).resolve(filename);
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("썸네일 파일이 존재하지 않거나 읽을 수 없습니다: {}", filePath);
            throw new RuntimeException("썸네일 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank())
            return "jpg";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1 && dotIndex < filename.length() - 1)
                ? filename.substring(dotIndex + 1).toLowerCase()
                : "jpg";
    }

    private void validateFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("파일 이름에 잘못된 경로 문자가 포함되어 있습니다.");
        }

        Path path = Paths.get(filename).normalize();
        if (path.startsWith("..") || path.isAbsolute()) {
            throw new IllegalArgumentException("파일 경로가 유효하지 않습니다.");
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 30MB를 초과할 수 없습니다.");
        }
    }
}
