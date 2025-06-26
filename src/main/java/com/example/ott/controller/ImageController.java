package com.example.ott.controller;

import com.example.ott.entity.Image;
import com.example.ott.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;

@Controller
@RequestMapping("/images")
@RequiredArgsConstructor
@Log4j2
public class ImageController {

    private final ImageService imageService;

    /** 업로드 폼 페이지 */
    @GetMapping("/image")
    public String showUploadForm() {
        return "uploads/upload"; // templates/uploads/upload.html
    }

    /** 이미지 업로드 처리 */
    @PostMapping("/image")
    public String handleImageUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            Image savedImage = imageService.uploadImageWithThumbnail(file);

            // FlashAttribute로 메시지 및 업로드된 이미지 정보 전달
            redirectAttributes.addFlashAttribute("message", "이미지 업로드 성공");
            redirectAttributes.addFlashAttribute("savedFilename", savedImage.getPath());
            redirectAttributes.addFlashAttribute("originalFilename", savedImage.getImgName());

        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            redirectAttributes.addFlashAttribute("error", "❌ 업로드 실패: " + e.getMessage());
        }
        return "redirect:/images/image";
    }

    /** 원본 이미지 제공 */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Resource resource = imageService.getImageFile(filename);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            contentType = (contentType != null) ? contentType : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException | RuntimeException e) {
            log.error("이미지 제공 실패: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** 썸네일 이미지 제공 (선택사항) */
    @GetMapping("/thumb/{filename:.+}")
    public ResponseEntity<Resource> serveThumbnail(@PathVariable String filename) {
        try {
            Resource resource = imageService.getThumbnailFile(filename);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            contentType = (contentType != null) ? contentType : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException | RuntimeException e) {
            log.error("썸네일 이미지 제공 실패: {}", filename, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
