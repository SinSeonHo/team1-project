package com.example.ott.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.User;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.ReplyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final MovieRepository movieRepository;

    public Reply insert(ReplyDTO dto) {
        Reply reply = Reply.builder()
                .movie(Movie.builder().mid(dto.getMno()).build())
                .text(dto.getText())
                .replyer(User.builder().id(dto.getReplyer()).build())
                .build();
        return replyRepository.save(reply);
    }

    public Reply rereplyInsert(ReplyDTO dto) {

        if (replyRepository.findById(dto.getRef()).isPresent()) {
            Reply reply = Reply.builder()
                    .movie(Movie.builder().mid(dto.getMno()).build())
                    .text(dto.getText())
                    .replyer(User.builder().id(dto.getReplyer()).build())
                    .ref(dto.getRef())
                    .build();
            return replyRepository.save(reply);
        }
        return null;
    }

    public List<Reply> selectReplies(Long rno) {
        Movie movie = movieRepository.findById(rno).get();
        List<Reply> list = replyRepository.findByMovie(movie);
        return list;
    }
}
