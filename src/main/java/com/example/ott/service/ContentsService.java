package com.example.ott.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.ott.dto.ContentRecommendation;
import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsGenre;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.UserGenrePreferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final ContentsGenreService contentsGenreService;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;
    private final UserGenrePreferenceRepository userGenrePreferenceRepository;

    // api로 불러온 콘텐츠 추가 및 장르 추가
    public void insertContents(ContentsDTO contentsDTO) {

        boolean hasContents = contentsRepository.existsByContentsId(contentsDTO.getContentsId());

        if (hasContents) {
            // 이미 존재하는 콘텐츠 일 경우
            return;
        } else {
            // 존재하지 않는 콘텐츠 일 경우
            Contents contents = Contents.builder()
                    .contentsId(contentsDTO.getContentsId())
                    .contentsType((contentsDTO.getContentsType() == ContentsType.GAME) ? ContentsType.GAME
                            : ContentsType.MOVIE)
                    .title(contentsDTO.getTitle())
                    .build();

            // contents_ID가 영화&게임 중 어떤것인지에 따라 관계 맺기
            switch (contentsDTO.getContentsType()) {
                case MOVIE:
                    Movie movie = movieRepository.findById(contentsDTO.getContentsId()).get();
                    contents.setMovie(movie);
                    break;

                case GAME:
                    Game game = gameRepository.findById(contentsDTO.getContentsId()).get();
                    contents.setGame(game);
                    break;
                default:
                    break;
            }

            contents = contentsRepository.save(contents);

            // 장르 추가
            contentsGenreService.insertContentsGenre(contents.getContentsId(), contentsDTO.getGenreNames());
        }

    }

    // public int getFollowCnt(String id) {
    // Contents contents = contentsRepository.findByContentsId(id)
    // .orElseThrow(() -> new NoSuchElementException("요청하신 콘텐츠는 존재하지 않는 콘텐츠입니다."));

    // return contents.getFollowCnt();
    // }

    public int getFollowCnt(String id) {
        return contentsRepository.findByContentsId(id)
                .map(Contents::getFollowCnt)
                .orElse(0);
    }

    public List<ContentsDTO> searchByGenre(String keyword) {
        List<ContentsGenre> genres = contentsGenreService.search(keyword);
        if (genres.size() > 0) {
            return genres.stream().map(c -> entityToDto(c.getContents())).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public PageResultDTO<ContentsDTO> search(PageRequestDTO pageRequestDTO) {
        Page<Contents> result = contentsRepository.search(pageRequestDTO);

        List<ContentsDTO> dtoList = result.stream().map(content -> entityToDto(content)).collect(Collectors.toList());

        return PageResultDTO.<ContentsDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(pageRequestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    private ContentsDTO entityToDto(Contents content) {
        if (content.getMovie() != null) {
            // 영화면
            ContentsDTO dto = ContentsDTO.builder()
                    .contentsId(content.getContentsId())
                    .contentsType(content.getContentsType())
                    .followCnt(content.getFollowCnt())
                    .title(content.getTitle())
                    .genres(movieRepository.findById(content.getContentsId()).get().getGenres())
                    .ranking(content.getMovie().getRanking())
                    .build();
            dto.setReplyCnt(content.getMovie().getReplies().size());
            return dto;
        } else if (content.getGame() != null) {
            // 게임이면
            ContentsDTO dto = ContentsDTO.builder()
                    .contentsId(content.getContentsId())
                    .contentsType(content.getContentsType())
                    .followCnt(content.getFollowCnt())
                    .title(content.getTitle())
                    .genres(gameRepository.findById(content.getContentsId()).get().getGenres())
                    .ranking(content.getGame().getRanking())
                    .build();
            dto.setImgUrl(content.getGame().getImage().getPath());
            return dto;
        }
        return null;
    }

    // 콘텐츠 수 반환
    public long getContentsCnt() {
        return contentsRepository.count();
    }

    // 팔로우 기반 추천(or 랜덤)
    public List<ContentsDTO> getRecommendContents(String userId) {
        boolean hasId = false;
        if (userId == null) hasId = false;

        // 0) 추천 조회
        List<ContentRecommendation> recs = Optional
                .ofNullable(userGenrePreferenceRepository.recommendByUserPreference(userId))
                .orElse(Collections.emptyList());

        final int NEED = 2; // 필요 개수
        final int THRESHOLD = 2; // 점수 컷

        // 1) 추천리스트 받아오기
        List<String> topIds = recs.stream()
                .filter(r -> r.getScore() != null && r.getScore() >= THRESHOLD)
                .map(ContentRecommendation::getContentsId)
                .limit(NEED)
                .toList();

        List<Contents> result = new ArrayList<>();
        if (!topIds.isEmpty() || hasId) {
            // 추천 리스트가 충분할경우 리턴
            List<Contents> fetched = contentsRepository.findAllById(topIds);
            Map<String, Contents> byId = fetched.stream()
                    .collect(Collectors.toMap(Contents::getContentsId, Function.identity()));
            for (String cid : topIds) {
                Contents c = byId.get(cid);
                if (c != null)
                    result.add(c);
            }
        }

        // 2) 부족하면 랜덤으로 채우기 (중복 제외)
        int need = NEED - result.size();
        if (need > 0 || hasId) {
            Set<String> exclude = result.stream()
                    .map(Contents::getContentsId)
                    .collect(Collectors.toSet());

            List<Contents> pool = contentsRepository
                    .pickRandom(PageRequest.of(0, need * 2));

            for (Contents c : pool) {
                if (c == null)
                    continue;
                if (!exclude.add(c.getContentsId()))
                    continue;
                result.add(c);
                if (result.size() >= NEED)
                    break;
            }
        }
        List<ContentsDTO> dtos = result.stream().map(r -> entityToDto(r)).collect(Collectors.toList());
        return dtos;
    }

}
