package com.example.ott.controller;

import java.util.List;

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

    @GetMapping("/movie/{mid}")
    public List<ReplyDTO> getMovieReplies(@PathVariable String mid) {
        return replyService.movieReplies(mid);
    }

    @PutMapping("/update")
    public ReplyDTO putReply(@RequestBody ReplyDTO dto) {
        return replyService.updateReply(dto);
    }

    @PostMapping("/new")
    public void postMovie(@RequestBody ReplyDTO dto) {
        User user = userService.getUser(dto.getReplyer());
        dto.setReplyerNickname(user.getNickname());
        replyService.insert(dto);

        return;
    }

    @DeleteMapping("/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
}
