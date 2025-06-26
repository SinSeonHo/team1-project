package com.example.ott.service;

import java.io.IOException;
import java.nio.file.Path;

public interface ThumbnailService {
    /**
     * 원본 파일에서 썸네일 생성
     * 
     * @param originalPath  원본 이미지 경로
     * @param thumbnailPath 생성할 썸네일 경로
     */
    void generateThumbnail(Path originalPath, Path thumbnailPath) throws IOException;
}
