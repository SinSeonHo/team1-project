package com.example.ott.service;

import com.example.ott.entity.WebToon;
import com.example.ott.repository.WebToonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebToonService {

    private final WebToonRepository webToonRepository;

    // 웹툰 등록
    public WebToon register(WebToon webToon) {
        return webToonRepository.save(webToon);
    }

    // 웹툰 단건 조회
    public Optional<WebToon> get(Long wid) {
        return webToonRepository.findById(wid);
    }

    // 전체 웹툰 목록 조회
    public List<WebToon> getAll() {
        return webToonRepository.findAll();
    }

    // 웹툰 삭제
    public void delete(Long wid) {
        webToonRepository.deleteById(wid);
    }

    // 웹툰 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public WebToon update(WebToon webToon) {
        return webToonRepository.save(webToon); // ID가 있으면 update
    }
}