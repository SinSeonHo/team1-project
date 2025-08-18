package com.example.ott.dto;

import com.example.ott.type.Status;

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
    // 댓글단 유저 닉네임
    private String replyerNickname;
    // 영화 아이디
    private String id;
    // 게임 아이디
    private String gid;

    private Long ref;
    // 부모(대댓글이 달린) 댓글의 유저명
    private String mention;

    private String createdDate;
    private String updatedDate;

    // 프사 경로
    private String thumbnailPath;
    // 뱃지 경로
    private String badgePath;

    @Builder.Default
    private Status status = Status.NO_ACTION;
}
