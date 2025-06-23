package com.example.ott.controller;

import com.example.ott.dto.ImageDTO;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 영화에 이미지 저장 요청 (예: POST /api/images/movie/{movieId})
     */
    @PostMapping("/movie/{movieId}")
    public ResponseEntity<ImageDTO> uploadMovieImage(
            @PathVariable String mid,
            @RequestParam String uuid,
            @RequestParam String imgName,
            @RequestParam String path) {

        ImageDTO savedImage = imageService.saveImageForMovie(mid, uuid, imgName, path);

        return ResponseEntity.ok(savedImage);
    }
}