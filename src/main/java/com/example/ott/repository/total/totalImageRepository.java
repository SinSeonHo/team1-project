package com.example.ott.repository.total;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface totalImageRepository {

    Page<Object[]> getTotalList(String type, String keyword, Pageable pageable);

    List<Object[]> getMovieRow(Long mno);
}
