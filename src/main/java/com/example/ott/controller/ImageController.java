package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    /** 1. 원본 이미지 업로드 폼 */
    @GetMapping("/image")
    public String showUploadForm(Model model) {
        return "uploads/upload"; // templates/uploads/upload.html
    }

    /** 2. 원본 이미지 업로드 처리 (썸네일 생성 없음) */
    @PostMapping("/image")
    public String handleImageUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            Image savedImage = imageService.uploadOriginalImage(file); // 원본 이미지 저장
            redirectAttributes.addFlashAttribute("image", savedImage);
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
        }
        return "redirect:/images/image";
    }

    /** 3. 원본 이미지 파일 반환 */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.getImageFile(filename);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            log.error("이미지 파일 제공 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
