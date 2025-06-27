package com.example.ott.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ReplyDTO {

    private Long rno;
    private String text;

    private int recommend;

    private String replyer;
    private String replyerNickname;

    private String mid;

    private String gid;

    private String wid;

    private Long ref;

    private String mention;

    private String createdDate;
    private String updatedDate;

    private String thumbnailPath;
}
