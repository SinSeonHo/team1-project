package com.example.ott.repository.search;

import org.springframework.data.domain.Page;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Game;

public interface GameSearchRepository {
    Page<Game> search(PageRequestDTO pagerequestDTO);
}
