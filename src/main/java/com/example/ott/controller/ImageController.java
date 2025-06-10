package com.example.ott.controller;

import java.io.IOException;
import java.util.List;

import com.example.ott.entity.Image;
import com.example.ott.repository.ImageRepository;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public FileController(ImageService imageService, ImageRepository imageRepository) {
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    // 1) 이미지 업로드
    @PostMapping
    public ResponseEntity<ImageMeta> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 실제 저장 & URL 경로 얻기
        String urlPath = ImageService.store(file);

        // DB에 메타정보 저장
        Image imageRepository = new ImageMeta();
        imageRepository.setFileName(file.getOriginalFilename());
        imageRepository.setUrlPath(urlPath);
        imageRepository.setContentType(file.getContentType());
        Image saved = imageRepository.save(imageRepository);

        return ResponseEntity.ok(saved);
    }

    // 2) 저장된 이미지 메타 목록 조회
    @GetMapping
    public List<Image> list() {
        return imageRepository.findAll();
    }
}
