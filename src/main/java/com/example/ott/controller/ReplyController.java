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
import com.example.ott.service.ReplyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/replies")
public class ReplyController {
    private final ReplyService replyService;

    @GetMapping("/movie/{mid}")
    public List<ReplyDTO> getMovieReplies(@PathVariable String mid) {
        log.info("{}번 영화 댓글 요청", mid);
        return replyService.movieReplies(mid);
    }

    @PutMapping("/movie/update")
    public ReplyDTO putReply(@RequestBody ReplyDTO dto) {
        log.info("댓글 내용 수정 요청: {}", dto);

        return replyService.updateReply(dto);
    }

    @PostMapping("/movie/new")
    public Long postMovie(@RequestBody ReplyDTO dto) {
        log.info("댓글 추가 요청: {}", dto);
        return replyService.insert(dto).getRno();
    }

    @PostMapping("/movie/newRe")
    public Long postMovieRecoment(@RequestBody ReplyDTO dto) {
        log.info("대댓글 추가 요청: {}", dto);
        return replyService.rereplyInsert(dto).getRno();
    }

    @DeleteMapping("/movie/{id}")
    public void deleteReply(@PathVariable Long id) {
        replyService.deleteReply(id);
    }
}
