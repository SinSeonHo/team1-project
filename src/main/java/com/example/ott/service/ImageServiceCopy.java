// package com.example.ott.service;

// import com.example.ott.entity.Image;
// import com.example.ott.repository.ImageRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.log4j.Log4j2;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import java.io.IOException;
// import java.nio.file.*;

// @Service
// @RequiredArgsConstructor
// @Log4j2

// // 썸네일과 이미지서비스를 분리 시켜둔 코드 아직 완성되지 않았습니다.
// // 추후에 썸네일 생성 로직을 별도의 서비스로 분리할 예정입니다.
// // 현재는 ImageService에서 썸네일 생성 로직을 포함하고 있습니다.
// // public class ThumbnailService {

// /**
// * 이미지 서비스에서 썸네일 생성 로직을 분리하여
// * ImageServiceCopy로 구현하였습니다.
// * 추후에 ThumbnailService로 분리할 예정입니다.
// */

// public class ImageServiceCopy {

// private final ImageRepository imageRepository;
// private final ThumbnailService thumbnailService;

// @Value("${upload.base-dir}")
// private String baseDir;

// private final String thumbnailDirName = "thumbnails";

// public void createThumbnail(Image image) throws IOException {
// if (image.getPath() == null || image.getPath().isBlank()) {
// throw new IllegalArgumentException("원본 이미지 경로가 없습니다.");
// }

// Path original = Paths.get(baseDir).resolve(image.getPath());
// if (!Files.exists(original)) {
// throw new IOException("원본 파일이 존재하지 않습니다: " + original);
// }

// // 썸네일 경로 준비
// String ext = getFileExtension(image.getPath());
// String thumbName = image.getUuid() + "_thumb." + ext;
// Path thumbFolder = Paths.get(baseDir, thumbnailDirName);
// Files.createDirectories(thumbFolder);
// Path thumbPath = thumbFolder.resolve(thumbName);

// // 썸네일 생성 실행 분리된 서비스에 위임
// thumbnailService.generateThumbnail(original, thumbPath);

// // DB에 새 썸네일 경로 반영
// image.setThumbnailPath(thumbnailDirName + "/" + thumbName);
// imageRepository.save(image);
// }

// // getFileExtension 등 다른 메서드는 그대로 유지...
// }
