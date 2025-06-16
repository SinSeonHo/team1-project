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
    // 댓글의 추천수 지금은 안씀
    private int recommend;
    // 댓글단 유저 아이디
    private String replyer;
    // 영화 아이디
    private String mid;
    // 게임 아이디
    private String gid;
    // 부모(대댓글이 달린) 댓글의 id
    private Long ref;
    // 부모(대댓글이 달린) 댓글의 유저명
    private String mention;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
