package com.example.ott.repository.search;

import org.springframework.data.domain.Page;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Contents;

public interface ContentsSearchRepository {
    Page<Contents> search(PageRequestDTO pagerequestDTO);

}
