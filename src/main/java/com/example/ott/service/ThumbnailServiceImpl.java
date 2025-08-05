package com.example.ott.service;

import java.io.IOException;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ThumbnailServiceImpl implements ThumbnailService {

    @Override
    public void generateThumbnail(Path originalPath, Path thumbnailPath) throws IOException {
        Thumbnails.of(originalPath.toFile())
                .size(200, 200)
                .toFile(thumbnailPath.toFile());
    }
}
