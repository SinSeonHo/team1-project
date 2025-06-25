package com.example.ott.service;

import com.example.ott.dto.ImageDTO;
import com.example.ott.entity.Image;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService {

        private final ImageRepository imageRepository;
        private final MovieRepository movieRepository;

        /**
         * 이미지 정보를 저장하고, 해당 영화를 이미지와 연결하는 메서드
         */
        @Transactional
        public ImageDTO saveImageForMovie(String mid, String uuid, String imgName, String path) {
                // 1. 영화 엔티티 조회 (movieId는 Long 또는 String 타입 mid에 맞게 조정)
                Movie movie = movieRepository.findById(mid)
                                .orElseThrow(() -> new IllegalArgumentException("영화를 찾을 수 없습니다. id=" + mid));

                // 2. Image 엔티티 생성 및 저장
                Image image = Image.builder()
                                .uuid(uuid)
                                .imgName(imgName)
                                .path(path)
                                .movie(movie) // 연관관계 설정
                                .build();

                Image savedImage = imageRepository.save(image);

                // 3. 영화 엔티티에 이미지 설정 후 저장 (양방향 연관관계 시 필요)
                movie.setImage(savedImage);
                movieRepository.save(movie);

                // 4. DTO 변환 및 반환
                ImageDTO dto = ImageDTO.builder()
                                .inum(savedImage.getInum())
                                .uuid(savedImage.getUuid())
                                .imgName(savedImage.getImgName())
                                .path(savedImage.getPath())
                                .build();

                return dto;
        }
}