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
        return replyRepository.save(dtoToEntity(dto));
    }

    public Reply rereplyInsert(ReplyDTO dto) {

        if (dto.getRef() != null) {
            // Reply reply = Reply.builder()
            // .movie(Movie.builder().mid(dto.getMno()).build())
            // .text(dto.getText())
            // .replyer(User.builder().id(dto.getReplyer()).build())
            // .ref(dto.getRef())
            // .build();
            return replyRepository.save(dtoToEntity(dto));
        }
        return null;
    }

    // 영화의 댓글들 가져오기
    public List<ReplyDTO> movieReplies(String mid) {
        Movie movie = movieRepository.findById(mid).get();
        List<Reply> list = replyRepository.findByMovie(movie);
        List<ReplyDTO> result = list.stream().map(reply -> entityToDto(reply))
                .collect(Collectors.toList());
        return result;
    }

    // 댓글 내용 변경
    public ReplyDTO updateReply(ReplyDTO dto) {
        Reply reply = replyRepository.findById(dto.getRno()).get();
        reply.changeText(dto.getText());

        return entityToDto(replyRepository.save(reply));
    }

    public void deleteReply(Long id) {
        replyRepository.deleteById(id);
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
        // if (reply.getMention() != null) {
        dto.setMention(reply.getMention());
        // }
        return dto;
    }

    private Reply dtoToEntity(ReplyDTO dto) {
        Reply reply = Reply.builder()
                .rno(dto.getRno())
                .text(dto.getText())
                .replyer(User.builder().id(dto.getReplyer()).build())
                .movie(Movie.builder().mid(dto.getMid()).build())
                .build();

        // 대댓글이면
        if (dto.getRef() != null) {
            // if (replyRepository.findById(dto.getRef()).isPresent()) {
            reply.setRef(dto.getRef());
            reply.setMention(dto.getMention());
        }
        return reply;
    }
}
