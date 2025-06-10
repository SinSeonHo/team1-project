package com.example.ott.service;

import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImageService {

    private final ImageRepository imageRepository;
    private final MovieRepository movieRepository;

    /**
     * application.properties 등에 설정된 업로드 기본 디렉터리(절대경로) 주입
     * 예: upload.base-dir=/var/app/uploads
     */
    @Value("${upload.base-dir}")
    private String baseDir;

    /**
     * 이미지 업로드 처리 및 DB 저장
     *
     * @param file    업로드된 MultipartFile
     * @param movieId 연관 저장할 Movie ID (null 허용 시, 없으면 movie 관계 설정 안 함)
     * @param ord     이미지 순서값 (0 이상의 정수; 필요 없다면 0으로 넘기면 됨)
     * @return 저장된 Image 엔티티
     * @throws IOException              파일 저장 중 예외 발생 시 던짐
     * @throws IllegalArgumentException 파일명 등이 유효하지 않을 때 던질 수 있음
     */
    public Image uploadAndSave(MultipartFile file, Long movieId, int ord) throws IOException {
        // 1. 원본 파일명 유효성 검사
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }

        // 2. 확장자 안전 추출
        int idx = originalFileName.lastIndexOf('.');
        String extension = "";
        if (idx != -1 && idx < originalFileName.length() - 1) {
            extension = originalFileName.substring(idx);
        }
        // 필요 시 허용 확장자 체크 로직을 추가할 수 있음

        // 3. UUID 및 저장 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String savedFileName = uuid + extension;

        // 4. 오늘 날짜 폴더 결정 (예: "2025-06-10")
        String dateFolder = LocalDate.now().toString();

        // 5. 실제 파일 시스템에 저장할 절대 디렉터리 경로
        // 예: baseDir + "/" + dateFolder
        Path saveFolder = Paths.get(baseDir, dateFolder);
        try {
            Files.createDirectories(saveFolder);
        } catch (IOException e) {
            log.error("업로드 폴더 생성 실패: {}", saveFolder, e);
            throw e;
        }

        // 6. 실제 파일 저장
        Path targetPath = saveFolder.resolve(savedFileName);
        try {
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", targetPath, e);
            throw e;
        }

        // 7. DB에 저장할 Image 엔티티 빌드
        // DB에는 baseDir 제외한 상대 경로(예: "2025-06-10/uuid.jpg")를 저장하면, 환경별 baseDir이 달라도 일관된
        // URL 매핑이 가능
        String relativePath = dateFolder + "/" + savedFileName;

        Image.ImageBuilder builder = Image.builder()
                .uuid(uuid)
                .imgName(originalFileName)
                .path(relativePath)
                .ord(ord);

        // 8. movieId가 주어졌으면 연관 Movie 엔티티 설정
        if (movieId != null) {
            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 movieId가 존재하지 않습니다: " +
                            movieId));
            builder.movie(movie);
        }

        Image image = builder.build();

        // 9. DB 저장
        Image saved = imageRepository.save(image);
        log.info("이미지 업로드 및 저장 완료: inum={}, originalName={}, savedPath={}",
                saved.getInum(), originalFileName, relativePath);
        return saved;
    }

    /**
     * 저장된 이미지 파일을 Resource 형태로 반환
     * 예: 컨트롤러에서 ResponseEntity<Resource> 로 반환하여 클라이언트가 이미지 요청할 수 있게 함
     *
     * @param inum Image 엔티티 PK
     * @return FileSystemResource (baseDir + "/" + image.getPath())
     * @throws RuntimeException (or custom exception) 이미지가 없거나 파일이 존재하지 않을 때
     */
    public Resource loadImageAsResource(Long inum) {
        Image image = imageRepository.findById(inum)
                .orElseThrow(() -> new RuntimeException("Image not found for inum: " +
                        inum));

        // FileSystemResource를 생성할 때 절대경로를 사용
        Path filePath = Paths.get(baseDir).resolve(image.getPath());
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            log.error("이미지 파일을 찾을 수 없음 또는 읽기 불가: {}", filePath);
            throw new RuntimeException("이미지 파일을 찾을 수 없습니다.");
        }
        return new FileSystemResource(filePath);
    }

    /**
     * 특정 Movie에 속한 모든 이미지 리스트 조회
     * 
     * @param movieId 조회할 Movie ID
     * @return List<Image>
     */

    /**
     * 이미지 삭제: DB 레코드 삭제 + 파일 시스템에서 실제 파일 삭제
     * 
     * @param inum 삭제할 Image PK
     * @throws IOException 파일 삭제 실패 시
     */
    public void deleteImage(Long inum) throws IOException {
        Image image = imageRepository.findById(inum)
                .orElseThrow(() -> new RuntimeException("Image not found for inum: " +
                        inum));

        // 1) 파일 시스템에서 삭제
        Path filePath = Paths.get(baseDir).resolve(image.getPath());
        try {
            Files.deleteIfExists(filePath);
            log.info("이미지 파일 삭제됨: {}", filePath);
        } catch (IOException e) {
            log.error("파일 시스템에서 이미지 삭제 실패: {}", filePath, e);
            throw e;
        }

        // 2) DB에서 레코드 삭제
        imageRepository.delete(image);
        log.info("이미지 엔티티 삭제됨: inum={}", inum);
    }
}
