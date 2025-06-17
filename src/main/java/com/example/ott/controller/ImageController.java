package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<Image> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        // ord 제거: 기본값 0으로 처리하거나, 서비스에서 설정
        Image image = imageService.uploadImage(file); // 또는 메서드를 오버로딩해서 ord 없이도 동작하도록 변경
        return ResponseEntity.ok(image);
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        Resource image = imageService.getImageFile(filename);

        if (!image.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedName + "\"")
                .contentType(MediaType.IMAGE_JPEG) // 확장자 따라 변경 필요
                .body(image);
    }
}
