package com.example.ott.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/replies")
public class ReplyController {
    private final ReplyService replyService;
    private final UserService userService;

    @GetMapping
    public String replyform(Model model) {
        model.addAttribute("m");
        return "ott_contents/movieInfo::replyform";
    }

    @PutMapping("/update")
    public ReplyDTO putReply(@RequestBody ReplyDTO dto) {
        return replyService.updateReply(dto);
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> postMovie(@RequestBody ReplyDTO dto) {
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
