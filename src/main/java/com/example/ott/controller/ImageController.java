package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/upload")
    public String showUploadPage() {
        return "uploads/upload"; // templates/upload-form.html
    }

    // @PostMapping("/upload")
    // public ResponseEntity<Image> uploadImage(
    // @RequestParam("file") MultipartFile file) throws IOException {
    // // ord 제거: 기본값 0으로 처리하거나, 서비스에서 설정
    // Image image = imageService.uploadImage(file); // 또는 메서드를 오버로딩해서 ord 없이도 동작하도록
    // 변경
    // return ResponseEntity.ok(image);
    // }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            imageService.uploadImages(file); // 실제 저장과 DB 등록 수행
            return ResponseEntity.ok("업로드 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업로드 실패");
        }
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
