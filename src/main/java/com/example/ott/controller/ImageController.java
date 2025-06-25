package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Log4j2
public class ImageController {

    private final ImageService imageService;

    /** 1. 이미지 업로드 (원본 + 썸네일 생성) */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image saved = imageService.uploadImageWithThumbnail(file);
            return ResponseEntity.ok(saved); // 저장된 이미지 정보 반환
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            log.error("파일 저장 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 중 오류 발생");
        }
    }

    /** 2. 원본 이미지 조회 */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.getImageFile(filename);
            String contentType = Files.probeContentType(resource.getFile().toPath());

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .body(resource);

        } catch (IOException e) {
            log.error("파일 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 3. 썸네일 이미지 조회 */
    @GetMapping("/thumbnail/{filename:.+}")
    public ResponseEntity<Resource> viewThumbnail(@PathVariable String filename) {
        try {
            Resource resource = imageService.getThumbnailFile(filename);
            String contentType = Files.probeContentType(resource.getFile().toPath());

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .body(resource);

        } catch (IOException e) {
            log.error("썸네일 파일 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
