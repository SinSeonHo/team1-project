package com.example.ott.service;

import java.io.IOException;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {

    @Override
    public void generateThumbnail(Path originalPath, Path thumbnailPath) throws IOException {
        Thumbnails.of(originalPath.toFile())
                .size(200, 200)
                .toFile(thumbnailPath.toFile());
        log.info("✅ 썸네일 생성 성공: {}", thumbnailPath);
    }
}
