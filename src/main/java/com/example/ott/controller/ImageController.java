package com.example.ott.controller;

import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/upload")
    public String showUploadPage() {
        return "uploads/upload"; // templates/upload-form.html
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            imageService.uploadImages(file);
            redirectAttributes.addFlashAttribute("message", "업로드 성공!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "업로드 실패: " + e.getMessage());
        }
        return "redirect:/images/upload"; // 업로드 페이지로 다시 이동
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
