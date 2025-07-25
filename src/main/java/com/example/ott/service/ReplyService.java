package com.example.ott.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.User;
import com.example.ott.entity.WebToon;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.ReplyRepository;
import com.example.ott.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    private boolean exist(Reply reply, ReplyDTO dto) {
        // dto의 영화에 작성된 댓글이 있는지 확인
        // if (reply.getMovie() != null ){
        // return (reply.getMovie().getMid() == dto.getId()) ? true: false;
        // }
        // dto의 게임에 작성된 댓글이 있는지 확인
        return false;
    }

    public Reply insert(ReplyDTO dto) {
        // 이미 댓글 단 컨텐츠인지 확인
        // Optional<User> user = userRepository.findById(dto.getMention());
        // if (user.isPresent()) {
        // replyRepository.findByReplyer(user.get()).stream().anyMatch(reply ->
        // exist(reply,
        // dto));
        // return replyRepository.save(dtoToEntityInsert(dto));
        // }
        return replyRepository.save(dtoToEntityInsert(dto));
    }

    // 콘텐츠의 댓글들 가져오기
    public List<ReplyDTO> contentReplies(String id) {
        List<Reply> list = new ArrayList<>();
        if (id.contains("m")) {
            Movie movie = movieRepository.findById(id).get();
            list = replyRepository.findByMovie(movie);
        } else if (id.contains("g")) {
            Game game = gameRepository.findById(id).get();
            list = replyRepository.findByGame(game);
        }
        List<ReplyDTO> result = sortRepliesWithChildren(list).stream().map(reply -> entityToDto(reply))
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

        User user = userRepository.findById(reply.getReplyer().getId()).get();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = reply.getCreatedDate().format(formatter);
        String formattedUpDate = reply.getUpdatedDate().format(formatter);
        String thumbnailPath;
        if (reply.getReplyer().getImage() == null) {
            thumbnailPath = null;
        } else {
            thumbnailPath = reply.getReplyer().getImage().getThumbnailPath();
        }
        ReplyDTO dto = ReplyDTO.builder()
                .rno(reply.getRno())
                .text(reply.getText())
                .replyer(reply.getReplyer().getId())
                .replyerNickname(user.getNickname())
                .rate(reply.getRecommend())
                .ref(reply.getRef())
                .mention(reply.getMention())
                .createdDate(formattedDate)
                .updatedDate(formattedUpDate)
                .thumbnailPath(thumbnailPath)
                .build();
        if (reply.getMovie() != null) {
            dto.setId(reply.getMovie().getMid());
        } else if (reply.getGame() != null) {
            dto.setGid(reply.getGame().getGid());
        }
        return dto;
    }

    private Reply dtoToEntityInsert(ReplyDTO dto) {
        Movie movie = null;
        Game game = null;
        // 별점 제한
        if (dto.getRate() < 0) {
            dto.setRate(0);
        } else if (dto.getRate() > 5) {
            dto.setRate(5);
        }

        if (dto.getId() != null) {
            movie = Movie.builder().mid(dto.getId()).build();
        } else if (dto.getGid() != null) {
            game = Game.builder().gid(dto.getGid()).build();
        }
        Reply reply = Reply.builder()
                .text(dto.getText())
                .replyer(User.builder().id(dto.getReplyer()).build())
                .movie(movie)
                .game(game)
                .ref(dto.getRef())
                .mention(dto.getMention())
                .recommend(dto.getRate())
                .build();
        return reply;
    }

    public List<Reply> sortRepliesWithChildren(List<Reply> allReplies) {
        // Map<부모 ID, List<자식 댓글>>
        Map<Long, List<Reply>> childrenMap = new HashMap<>();
        List<Reply> topLevel = new ArrayList<>();

        // 댓글 분류: 최상위 vs 자식 댓글
        for (Reply reply : allReplies) {
            if (reply.getRef() == null) {
                topLevel.add(reply);
            } else {
                childrenMap
                        .computeIfAbsent(reply.getRef(), k -> new ArrayList<>())
                        .add(reply);
            }
        }

        // 날짜 순 정렬
        Comparator<Reply> byDate = Comparator.comparing(Reply::getCreatedDate);
        topLevel.sort(byDate);
        for (List<Reply> list : childrenMap.values()) {
            list.sort(byDate);
        }

        // 재귀적으로 계층을 평탄화
        List<Reply> sortedReplies = new ArrayList<>();
        for (Reply parent : topLevel) {
            addWithChildren(parent, childrenMap, sortedReplies);
        }

        return sortedReplies;
    }

    private void addWithChildren(Reply parent, Map<Long, List<Reply>> childrenMap, List<Reply> result) {
        result.add(parent);
        List<Reply> children = childrenMap.get(parent.getRno());
        if (children != null) {
            for (Reply child : children) {
                addWithChildren(child, childrenMap, result); // 재귀: 자식의 자식까지 정렬
            }
        }
    }

}
