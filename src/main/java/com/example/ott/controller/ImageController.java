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

    /** 1. 업로드 폼 보여주기 + 업로드 결과 표시 */
    @GetMapping("/image")
    public String showUploadForm(Model model) {
        // image, error 는 FlashAttribute로 전달됨
        return "uploads/upload"; // templates/uploads/upload.html
    }

    /** 2. 업로드 처리 (PRG 패턴 적용) */
    @PostMapping("/image")
    public String handleImageUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            Image savedImage = imageService.uploadImages(file);
            redirectAttributes.addFlashAttribute("image", savedImage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
        }
        return "redirect:/images/image"; // 새로고침 시 POST 재요청 방지
    }

    /** 3. 이미지 반환 (파일 경로 기반) */
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
            log.error("이미지 파일 제공 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
