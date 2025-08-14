package com.example.ott.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Data
@SuperBuilder
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 100;

    // 검색
    @Builder.Default
    private String type = "t";
    private String keyword;

    public Pageable getPageable(Sort sort) {
        return PageRequest.of(this.page - 1, this.size, sort);
    }

    public String[] getTypes() {
        return (type == null || type.isEmpty()) ? new String[] {} : type.split("");
    }
}
