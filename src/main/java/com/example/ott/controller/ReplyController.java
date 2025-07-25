package com.example.ott.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.User;
import com.example.ott.service.ReplyService;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/replies")
public class ReplyController {
    private final ReplyService replyService;
    private final UserService userService;

    @PutMapping("/update")
    public ReplyDTO putReply(@RequestBody ReplyDTO dto) {
        log.info("댓글 내용 수정 요청: {}", dto);

        return replyService.updateReply(dto);
    }

    @PostMapping("/new")
    public void postMovie(@RequestBody ReplyDTO dto) {
        log.info("댓글 추가 요청: {}", dto);
        User user = userService.getUser(dto.getReplyer());
        dto.setReplyerNickname(user.getNickname()); // nickname 설정
        replyService.insert(dto);

        return;
    }

    @DeleteMapping("/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
}
