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
@RequiredArgsConstructor
@RequestMapping("/thumbnail")
@Log4j2
public class ThumbnailController {

    private final ImageService imageService;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /** 1. 썸네일 생성 폼 */
    @GetMapping("/image")
    public String showThumbnailForm() {
        return "uploads/thumbnail";
    }

    /** 3. 썸네일 이미지 파일 직접 업로드 처리 (썸네일 전용 업로드) */
    @PostMapping("/upload")
    public String uploadThumbnailFile(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                redirectAttributes.addFlashAttribute("error", "파일 크기는 10MB를 초과할 수 없습니다.");
                return "redirect:/thumbnail/image";
            }

            Image savedThumbnail = imageService.uploadThumbnailImage(file);
            redirectAttributes.addFlashAttribute("message", "썸네일 업로드 및 생성 완료");
            redirectAttributes.addFlashAttribute("uploadedFilename", savedThumbnail.getThumbnailPath());

        } catch (Exception e) {
            log.error("썸네일 업로드 실패", e);
            redirectAttributes.addFlashAttribute("error", "썸네일 업로드 실패: " + e.getMessage());
        }
        return "redirect:/thumbnail/image";
    }

    /** 4. 썸네일 이미지 파일 제공 (뷰에서 보여주기용) */
    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> serveThumbnail(@PathVariable String filename) {
        try {
            Resource resource = imageService.getThumbnailFile(filename);
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("썸네일 파일을 찾을 수 없거나 읽을 수 없습니다: " + filename);
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("썸네일 파일 제공 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            log.error("썸네일 파일 제공 실패", e);
            return ResponseEntity.notFound().build();
        }
    }
}
