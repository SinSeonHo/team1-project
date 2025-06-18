package com.example.ott.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.User;
import com.example.ott.entity.WebToon;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.ReplyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final MovieRepository movieRepository;

    public Reply insert(ReplyDTO dto) {
        return replyRepository.save(dtoToEntityInsert(dto));
    }

    // public Reply rereplyInsert(ReplyDTO dto) {

    // if (dto.getRef() != null) {
    // // Reply reply = Reply.builder()
    // // .movie(Movie.builder().mid(dto.getMno()).build())
    // // .text(dto.getText())
    // // .replyer(User.builder().id(dto.getReplyer()).build())
    // // .ref(dto.getRef())
    // // .build();
    // return replyRepository.save(dtoToEntity(dto));
    // }
    // return null;
    // }

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

    @Transactional
    public void deleteReply(Long id) {
        List<Reply> list = replyRepository.findByRef(id);
        list.stream().forEach(rep -> replyRepository.deleteById(rep.getRno()));
        replyRepository.deleteById(id);
    }

    private ReplyDTO entityToDto(Reply reply) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = reply.getCreatedDate().format(formatter);
        ReplyDTO dto = ReplyDTO.builder()
                .rno(reply.getRno())
                .text(reply.getText())
                .replyer(reply.getReplyer().getName())
                .recommend(reply.getRecommend())
                .ref(reply.getRef())
                .mention(reply.getMention())
                .createdDate(formattedDate)
                .updatedDate(reply.getUpdatedDate())
                .build();
        if (reply.getMovie() != null) {
            dto.setMid(reply.getMovie().getMid());
        } else if (reply.getGame() != null) {
            dto.setGid(reply.getGame().getGid());
        }
        // 멘션이 있으면 추가해줌
        // if (reply.getMention() != null) {
        // dto.setMention(reply.getMention());
        // }
        return dto;
    }

    private Reply dtoToEntity(ReplyDTO dto) {

        Movie movie = null;
        Game game = null;
        WebToon webToon = null;

        if (dto.getMid() != null) {
            movie = Movie.builder().mid(dto.getMid()).build();
        } else if (dto.getGid() != null) {
            game = Game.builder().gid(dto.getGid()).build();
        } else {
            // webToon = WebToon.builder().wid(dto.getWid()).build();
        }

        Reply reply = Reply.builder()
                // .rno(dto.getRno())
                .text(dto.getText())
                .replyer(User.builder().id(dto.getReplyer()).build())
                .movie(movie)
                .game(game)
                // .webtoon(webToon)
                .ref(dto.getRef())
                .mention(dto.getMention())
                .build();
        return reply;
    }

    private Reply dtoToEntityInsert(ReplyDTO dto) {
        Movie movie = null;
        Game game = null;
        WebToon webToon = null;

        if (dto.getMid() != null) {
            movie = Movie.builder().mid(dto.getMid()).build();
        } else if (dto.getGid() != null) {
            game = Game.builder().gid(dto.getGid()).build();
        } else {
            // webToon = WebToon.builder().wid(dto.getWid()).build();
        }
        Reply reply = Reply.builder()
                .text(dto.getText())
                .replyer(User.builder().id(dto.getReplyer()).build())
                .movie(movie)
                .game(game)
                // .webtoon(webToon)
                .ref(dto.getRef())
                .mention(dto.getMention())
                .build();
        // 대댓글이면
        // if (dto.getRef() != null) {
        // reply.setRef(dto.getRef());
        // reply.setMention(dto.getMention());
        // }
        return reply;
    }

}
