package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @Value("${com.example.movie.upload.path}")
    private String uploadPath;

    /** 1. 업로드 폼 보여주기 */
    @GetMapping("/image")
    public String showUploadForm() {
        return "uploads/upload"; // templates/upload/image-form.html
    }

    /** 2. 업로드 처리 */
    @PostMapping("/image")
    public String handleImageUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            Image savedImage = imageService.uploadImages(file);
            model.addAttribute("image", savedImage);
        } catch (Exception e) {
            model.addAttribute("error", "이미지 업로드 실패: " + e.getMessage());
        }
        return "uploads/upload";
    }

    /** 3. 이미지 반환 (파일 경로 기반) */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.getImageFile(filename);

            // MIME 타입 자동 추출
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            log.error("이미지 파일 제공 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
