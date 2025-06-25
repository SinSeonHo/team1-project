package com.example.ott.service;

import com.example.ott.dto.ImageDTO;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Log4j2
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final MovieRepository movieRepository;

    @Transactional
    public ImageDTO saveImageForMovie(String mid, String uuid, String imgName, String path) {
        // 1. 영화 엔티티 조회 (movieId는 Long 또는 String 타입 mid에 맞게 조정)
        Movie movie = movieRepository.findById(mid)
                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다. id=" + mid));

        // 2. Image 엔티티 생성 및 저장
        Image image = Image.builder()
                .uuid(uuid)
                .imgName(imgName)
                .path(path)
                .movie(movie) // 연관관계 설정
                .build();

        Image savedImage = imageRepository.save(image);

        // 3. 영화 엔티티에 이미지 설정 후 저장 (양방향 연관관계 시 필요)
        movie.setImage(savedImage);
        movieRepository.save(movie);

        // 4. DTO 변환 및 반환
        ImageDTO dto = ImageDTO.builder()
                .inum(savedImage.getInum())
                .uuid(savedImage.getUuid())
                .imgName(savedImage.getImgName())
                .path(savedImage.getPath())
                .build();

        return dto;
    }

    @Value("${upload.base-dir}")
    private String baseDir;

    private final String thumbnailDirName = "thumbnails";
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024; // 30MB 제한

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
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 30MB를 초과할 수 없습니다.");
        }
    }
}
