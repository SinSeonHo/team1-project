package com.example.ott.service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 영화의 댓글들 가져오기
    public List<ReplyDTO> movieReplies(Long rno) {
        Movie movie = movieRepository.findById(rno).get();
        List<Reply> list = replyRepository.findByMovie(movie);
        List<ReplyDTO> result = list.stream().map(reply -> entityToDto(reply))
                .collect(Collectors.toList());
        return result;
    }

    public List<Reply> selectReplies(Long rno) {
        Movie movie = movieRepository.findById(rno).get();
        List<Reply> list = replyRepository.findByMovie(movie);
        return list;
    }

    private ReplyDTO entityToDto(Reply reply) {
        ReplyDTO dto = ReplyDTO.builder()
                .rno(reply.getRno())
                .text(reply.getText())
                .replyer(reply.getReplyer().getName())
                .recommend(reply.getRecommend())
                .ref(reply.getRef())
                .createdDate(reply.getCreatedDate())
                .updatedDate(reply.getUpdatedDate())
                .build();

        // 멘션이 있으면 추가해줌
        if (reply.getMention() != null) {
            dto.setMention(reply.getMention());
        }
        return dto;
    }
}
