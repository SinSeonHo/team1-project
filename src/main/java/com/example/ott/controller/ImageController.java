package com.example.ott.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

import com.example.ott.entity.Image;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.service.ImageService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageRepository imageRepository;
    private final MovieRepository movieRepository; // 필요 시

    private final ImageService imageService;

    @Value("${upload.base-dir}")
    private String baseDir; // application.properties에 설정

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "movieId", required = false) Long movieId,
            @RequestParam(value = "ord", required = false, defaultValue = "0") int ord) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            return ResponseEntity.badRequest().body("파일 이름이 유효하지 않습니다.");
        }
        int idx = originalFileName.lastIndexOf('.');
        String extension = "";
        if (idx != -1 && idx < originalFileName.length() - 1) {
            extension = originalFileName.substring(idx);
        }
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + extension;

        // 오늘 날짜 폴더
        String dateFolder = LocalDate.now().toString(); // "2025-06-10"
        // 절대 경로: baseDir/2025-06-10
        Path saveFolder = Paths.get(baseDir, dateFolder);
        try {
            Files.createDirectories(saveFolder);
            Path targetPath = saveFolder.resolve(savedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String fullPath = dateFolder + "/" + savedFileName;
            // DB에는 baseDir 없이, 클라이언트 요청 시 URL 매핑으로만 사용하도록 저장
            Image.ImageBuilder builder = Image.builder()
                    .uuid(uuid)
                    .imgName(originalFileName)
                    .path(fullPath)
                    .ord(ord);
            if (movieId != null) {
                movieRepository.findById(movieId).ifPresent(builder::movie);
            }
            Image saved = imageRepository.save(builder.build());
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            // 로깅 후 에러 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 저장 중 오류 발생");
        }
    }

}
