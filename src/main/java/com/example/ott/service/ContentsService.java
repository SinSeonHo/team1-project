package com.example.ott.service;

import java.util.NoSuchElementException;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ContentsRepository;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentsService {

    private final ContentsRepository contentsRepository;
    private final ContentsGenreService contentsGenreService;
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;

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
        ContentsDTO dto = ContentsDTO.builder()
                .contentsId(content.getContentsId())
                .contentsType(content.getContentsType())
                .followCnt(content.getFollowCnt())
                .title(content.getTitle())
                .build();
        if (content.getMovie() != null) {
            dto.setReplyCnt(content.getMovie().getReplies().size());
            if (content.getMovie().getImage() != null) {
                dto.setImgUrl(content.getMovie().getImage().getPath());
            }
        } else if (content.getGame() != null) {
            dto.setReplyCnt(content.getGame().getReplies().size());
            if (content.getGame().getImage() != null) {
                dto.setImgUrl(content.getGame().getImage().getPath());
            }
        } else {
            dto.setImgUrl(null);
        }
        return dto;
    }
}
