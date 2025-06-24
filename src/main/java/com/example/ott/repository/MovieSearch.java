package com.example.ott.repository;

import org.springframework.data.domain.Page;

import com.example.ott.dto.PageRequestDTO;
import com.example.ott.entity.Movie;

public interface MovieSearch {
    Page<Movie> search(PageRequestDTO pagerequestDTO);
}
