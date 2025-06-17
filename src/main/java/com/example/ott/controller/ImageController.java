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
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "ord", defaultValue = "0") int ord) throws IOException {

        Image image = imageService.uploadImage(file, ord);
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
                .contentType(MediaType.IMAGE_JPEG) // 또는 확장자에 따라 처리
                .body(image);
    }
}
