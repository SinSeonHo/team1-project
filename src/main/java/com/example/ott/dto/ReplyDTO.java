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
    // 댓글의 별점
    private int rate;
    // 댓글단 유저 아이디
    private String replyer;
    private String replyerNickname;
    // 영화 아이디
    private String id;
    // 게임 아이디
    private String gid;
    // 웹툰 아이디
    private String wid;

    // 부모(대댓글이 달린) 댓글의 id
    private Long ref;
    // 부모(대댓글이 달린) 댓글의 유저명
    private String mention;

    private String createdDate;
    private String updatedDate;

    private String thumbnailPath;
}
