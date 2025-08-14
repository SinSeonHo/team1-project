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
import com.example.ott.exception.ReportActionException;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.entity.Report;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.ReplyRepository;
import com.example.ott.repository.ReportRepository;
import com.example.ott.type.Status;

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
    private final UserService userService;
    private final ReportRepository reportRepository;

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
        List<ReplyDTO> result = sortRepliesWithChildren(list).stream().map(reply -> {
            // 신고된 댓글이 삭제처리 된 경우 내용이 대체됨.
            ReplyDTO dto = entityToDto(reply);
            if (dto.getStatus().equals(Status.DELETED)) {
                dto.setText("관리자에 의해 삭제된 게시물입니다.");
            }
            return dto;
        })
                .collect(Collectors.toList());
        return result;
    }

    // 댓글 내용 변경
    public ReplyDTO updateReply(ReplyDTO dto) {
        // 신고가 접수된 게시물은 수정할 수 없음
        Reply reply = replyRepository.findById(dto.getRno()).get();
        boolean isReported = reply.getStatus() != Status.NO_ACTION;
        log.info("=================================================================");
        log.info("isReported : {} \n dto의 status : {}", isReported, dto.getStatus());
        log.info("=================================================================");
        if (isReported) {
            throw new ReportActionException("신고가 접수된 게시물로 수정할 수 없습니다.");
        }

        reply.changeText(dto.getText());
        reply.changeRate(dto.getRate());

        return entityToDto(replyRepository.save(reply));
    }

    @Transactional
    public void deleteReply(Long id) {
        Reply reply = replyRepository.findById(id).get();
        List<Report> reports = reportRepository.findByReply(reply);
        reports.forEach(report -> {
            report.setReply(null);
            reportRepository.save(report);
        });
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
                .status(reply.getStatus())
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

    // 0812 신선호 신고기능때문에 추가
    // ID로 댓글 조회 (Optional 반환)
    public Optional<Reply> findById(Long id) {
        return replyRepository.findById(id);
    }

}
