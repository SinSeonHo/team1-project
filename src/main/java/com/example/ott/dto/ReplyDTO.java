package com.example.ott.dto;

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
    private String mno;
    private Long ref;
    private String mention;
}
