package com.example.ott.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.User;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.ReplyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;
    private final UserService userService;

    public int insert(ReplyDTO dto) {
        User user = userService.getUserById(dto.getReplyer());

        if (dto.getRef() == null) {
            if (dto.getId() != null) {
                Movie movie = movieRepository.findById(dto.getId())
                        .orElseThrow(() -> new RuntimeException("Movie not found"));
                if (replyRepository.findByReplyerAndMovieAndRefIsNull(user, movie).isPresent()) {
                    return 2; // 이미 리뷰를 작성했습니다.
                }
            } else if (dto.getGid() != null) {
                Game game = gameRepository.findById(dto.getGid())
                        .orElseThrow(() -> new RuntimeException("Game not found"));
                if (replyRepository.findByReplyerAndGameAndRefIsNull(user, game).isPresent()) {
                    return 2; // 이미 리뷰를 작성했습니다.
                }
            }
        }
        replyRepository.save(dtoToEntityInsert(dto));
        return 0;
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
        reply.changeRate(dto.getRate());

        return entityToDto(replyRepository.save(reply));
    }

    @Transactional
    public void deleteReply(Long id) {
        List<Reply> list = replyRepository.findByRef(id);
        list.stream().forEach(rep -> replyRepository.deleteById(rep.getRno()));
        replyRepository.deleteById(id);
    }

    private ReplyDTO entityToDto(Reply reply) {

        User user = userService.getUserById(reply.getReplyer().getId());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = reply.getCreatedDate().format(formatter);
        String formattedUpDate = reply.getUpdatedDate().format(formatter);
        String thumbnailPath, badge = null;

        if (reply.getReplyer().getImage() == null) {
            thumbnailPath = null;
        } else {
            thumbnailPath = reply.getReplyer().getImage().getThumbnailPath();
        }
        ReplyDTO dto = ReplyDTO.builder()
                .rno(reply.getRno())
                .text(reply.getText())
                .replyer(user.getId())
                .replyerNickname(user.getNickname())
                .rate(reply.getRecommend())
                .ref(reply.getRef())
                .mention(reply.getMention())
                .createdDate(formattedDate)
                .updatedDate(formattedUpDate)
                .thumbnailPath(thumbnailPath)
                .badgePath(badge)
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
        if (dto.getRef() == null) {
            if (dto.getRate() < 0) {
                dto.setRate(0);
            } else if (dto.getRate() > 5) {
                dto.setRate(5);
            }
        } else {
            dto.setRate(0);
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

    public double rating(List<ReplyDTO> replies) {
        double rating = 0;
        int size = 0;
        double rate = 0;
        for (ReplyDTO dto : replies) {
            if (dto.getRate() > 0) {
                rating += dto.getRate();
                size++;
            }
        }
        if (size > 0) {
            rate = rating / size;
            rate = Math.round(rate * 10) / 10.0;
        }
        return rate;
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
                addWithChildren(child, childrenMap, result);
            }
        }
    }
}
