package com.example.ott.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, String>> postMovie(@RequestBody ReplyDTO dto) {
        log.info("댓글 추가 요청: {}", dto);
        User user = userService.getUserById(dto.getReplyer());
        dto.setReplyerNickname(user.getNickname()); // nickname 설정
        int result = replyService.insert(dto);
        switch (result) {
            case 0:
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "입력되었습니다."));
            case 1:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "컨텐츠를 찾을 수 없습니다."));
            case 2:
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "이미 리뷰를 작성했습니다."));
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "오류"));
        }
    }

    @DeleteMapping("/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
}
