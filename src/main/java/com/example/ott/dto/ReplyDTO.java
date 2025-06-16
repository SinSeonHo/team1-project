package com.example.ott.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReplyDTO {

    private Long rno;
    private String text;
    private int recommend;
    private String replyer;

    private String mid;

    private Long ref; // 대댓글 id
    private String mention;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
