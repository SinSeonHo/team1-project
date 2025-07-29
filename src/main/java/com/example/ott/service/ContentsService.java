package com.example.ott.service;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.entity.Contents;
import com.example.ott.repository.ContentsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final ContentsGenreService contentsGenreService;

    // api로 불러온 콘텐츠 추가 및 장르 추가
    public void insertContents(ContentsDTO contentsDTO) {

        boolean hasContents = contentsRepository.existsByContentsId(contentsDTO.getContentsId());

        if (hasContents) {
            // 이미 존재하는 콘텐츠 일 경우
            return;
        } else {
            // 존재하지 않는 콘텐츠 일 경우
            Contents contents = Contents.builder()
                    .contentsId(contentsDTO.getContentsId())
                    .contentsType(contentsDTO.getContentsType())
                    .title(contentsDTO.getTitle())
                    .build();

            contents = contentsRepository.save(contents);

            // 장르 추가
            contentsGenreService.insertContentsGenre(contents.getContentsId(), contentsDTO.getGenreNames());
        }

    }

}
