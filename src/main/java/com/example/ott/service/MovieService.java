package com.example.ott.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Movie;
import com.example.ott.repository.MovieRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ReplyService replyService;
    private final ModelMapper modelMapper;

    @Scheduled(cron = "00 00 10 * * *") // 매일 오전10:00에 실행
    @Transactional
    public void scheduledMovieImport() {
        log.info("자동 영화 데이터 수집 시작");
        importMovies();
    }

    @Scheduled(cron = "00 01 10 * * *") // 매일 오전10:01에 실행
    @Transactional
    public void scheduledMovieSynopsisImport() {
        log.info("자동 영화 줄거리 및 포스터 반영");
        runPythonMovieCrawler();
    }

    public void runPythonMovieCrawler() {
        try {
            System.out.println("Python 크롤러 실행 시작");

            // 첫 번째 파이썬 스크립트 실행 (영화 줄거리 크롤러)
            ProcessBuilder pbSynopsis = new ProcessBuilder("python",
                    "C:/SOURCE/ott/python/movieSynopsisCrwal.py");
            Map<String, String> env = pbSynopsis.environment();
            env.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");
            Process processSynopsis = pbSynopsis.start();
            int exitCodeSynopsis = processSynopsis.waitFor();
            System.out.println("줄거리 크롤러 종료. Exit code: " + exitCodeSynopsis);

            if (exitCodeSynopsis == 0) {
                // 두 번째 파이썬 스크립트 실행 (영화 이미지 크롤러)
                ProcessBuilder pbImage = new ProcessBuilder("python",
                        "C:/SOURCE/ott/python/movieImageCrwal.py");
                Map<String, String> envImage = pbImage.environment();
                envImage.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");
                Process processImage = pbImage.start();
                int exitCodeImage = processImage.waitFor();
                System.out.println("이미지 크롤러 종료. Exit code: " + exitCodeImage);
            } else {
                System.err.println("줄거리 크롤러가 실패하여 이미지 크롤러를 실행하지 않습니다.");
            }

        } catch (Exception e) {
            System.err.println("파이썬 실행 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 영화 등록
    @Transactional
    public String insertMovie(MovieDTO dto) {
        log.info("db에 영화 저장");

        String lastId = movieRepository.findLastMovieId();
        int nextIdNum = 1;

        if (lastId != null && lastId.startsWith("m_")) {
            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
        }

        String mid = "m_" + nextIdNum;

        Movie movie = Movie.builder()
                .mid(mid)
                .title(dto.getTitle())
                .openDate(dto.getOpenDate())
                .rank(dto.getRank())
                .movieCd(dto.getMovieCd())
                .actors(dto.getActors())
                .director(dto.getDirector())
                .genres(dto.getGenres())
                .showTm(dto.getShowTm())
                .nationNm(dto.getNationNm())
                .gradeNm(dto.getGradeNm())
                .synopsis(dto.getSynopsis()) // synopsis 추가
                .build();

        movieRepository.save(movie);

        return movie.getMid();
    }

    // 줄거리만 업데이트하는 메서드 (필요시 호출)
    @Transactional
    public void updateSynopsis(String mid, String synopsis) {
        Movie movie = movieRepository.findById(mid)
                .orElseThrow(() -> new RuntimeException("영화 없음"));
        movie.setSynopsis(synopsis);
        movieRepository.save(movie);
    }

    // 영화진흥위원회 API 호출 및 insertMovie() 실행하여 DB에 저장
    @Transactional
    public void importMovies() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = yesterday.format(formatter);

        String apiUrl1 = "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json"
                + "?key=4cb94726cef5af841db6efd248a5af76"
                + "&targetDt=" + formattedDate;

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl1, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String json = response.getBody();

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(json);
                JsonNode movieList = root.path("boxOfficeResult").path("dailyBoxOfficeList");

                for (JsonNode movieNode : movieList) {
                    String movieCd = movieNode.get("movieCd").asText();
                    String movieNm = movieNode.get("movieNm").asText();
                    String openDt = movieNode.get("openDt").asText();
                    int rank = movieNode.get("rank").asInt();

                    String apiUrl2 = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json"
                            + "?key=4cb94726cef5af841db6efd248a5af76"
                            + "&movieCd=" + movieCd;

                    ResponseEntity<String> detailResponse = restTemplate.getForEntity(apiUrl2, String.class);
                    String detailJson = detailResponse.getBody();
                    JsonNode detailRoot = objectMapper.readTree(detailJson);
                    JsonNode movieInfo = detailRoot.path("movieInfoResult").path("movieInfo");

                    // 감독
                    JsonNode directors = movieInfo.path("directors");
                    String directorName = "[감독정보없음]";
                    if (directors.isArray() && directors.size() > 0) {
                        directorName = directors.get(0).path("peopleNm").asText();
                    }

                    // 배우
                    JsonNode actors = movieInfo.path("actors");
                    List<String> actorNames = new ArrayList<>();
                    for (JsonNode actorNode : actors) {
                        actorNames.add(actorNode.path("peopleNm").asText());
                    }
                    String actorStr = actorNames.isEmpty() ? "[배우정보없음]" : String.join(", ", actorNames);

                    // 장르
                    JsonNode genres = movieInfo.path("genres");
                    List<String> genreNames = new ArrayList<>();
                    for (JsonNode genreNode : genres) {
                        genreNames.add(genreNode.path("genreNm").asText());
                    }
                    String genreStr = genreNames.isEmpty() ? "[장르정보없음]" : String.join(", ", genreNames);

                    // 상영시간
                    int showTm = 0;
                    try {
                        showTm = Integer.parseInt(movieInfo.path("showTm").asText());
                    } catch (NumberFormatException e) {
                        showTm = 0;
                    }

                    // 제작국가
                    JsonNode nations = movieInfo.path("nations");
                    String nationNm = "[제작국가없음]";
                    if (nations.isArray() && nations.size() > 0) {
                        nationNm = nations.get(0).path("nationNm").asText();
                    }

                    // 관람등급
                    JsonNode audits = movieInfo.path("audits");
                    String gradeNm = "[등급정보없음]";
                    if (audits.isArray() && audits.size() > 0) {
                        gradeNm = audits.get(0).path("watchGradeNm").asText();
                    }

                    Optional<Movie> optionalMovie = movieRepository.findByMovieCd(movieCd);

                    if (optionalMovie.isPresent()) {
                        // update existing movie (synopsis는 업데이트 안함, 필요시 추가 가능)
                        Movie existing = optionalMovie.get();
                        existing.setRank(rank);
                        existing.setDirector(directorName);
                        existing.setActors(actorStr);
                        existing.setGenres(genreStr);
                        movieRepository.save(existing);
                    } else {
                        // insert new movie
                        MovieDTO dto = MovieDTO.builder()
                                .mid("m_" + movieCd)
                                .title(movieNm)
                                .openDate(openDt)
                                .rank(rank)
                                .movieCd(movieCd)
                                .director(directorName)
                                .actors(actorStr)
                                .genres(genreStr)
                                .showTm(showTm)
                                .nationNm(nationNm)
                                .gradeNm(gradeNm)
                                .synopsis(null) // 초기 줄거리 없음
                                .build();

                        insertMovie(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("영화 데이터 수집 실패", e);
        }
    }

    // 영화 단건 상세정보 + 댓글 리스트 조회
    public Map<String, Object> getMovie(String mid) {
        log.info("영화정보 상세조회");

        Movie movie = movieRepository.findById(mid)
                .orElseThrow(() -> new RuntimeException("영화 없음"));

        List<ReplyDTO> replyDTOList = replyService.movieReplies(mid);

        Map<String, Object> result = new HashMap<>();
        result.put("movie", movie);
        result.put("replies", replyDTOList);

        return result;
    }

    public PageResultDTO<MovieDTO> getSearch(PageRequestDTO requestDTO) {
        Page<Movie> result = movieRepository.search(requestDTO);

        List<MovieDTO> dtoList = result.stream()
                // .map(movie -> modelMapper.map(movie, MovieDTO.class))
                .map(movie -> entityToDto(movie))
                .collect(Collectors.toList());

        return PageResultDTO.<MovieDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(requestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    // 전체 영화 목록 조회
    public List<Movie> getMovieAll() {
        log.info("영화 전체목록 조회");
        return movieRepository.findAll();
    }

    // 영화 목록 조회
    public List<Movie> getMovieRank(int num) {
        List<Movie> result;
        List<Movie> list = movieRepository.findAll(Sort.by("rank"));
        if (list.size() > num) {
            result = new ArrayList<>(list.subList(0, num));
        } else {
            result = new ArrayList<>(list);
        }
        return result;
    }

    public List<MovieDTO> getRandom(int num) {
        List<MovieDTO> result;
        List<Movie> list = movieRepository.findAll();
        // result = list.stream()
        // .map(movie -> modelMapper.map(movie, MovieDTO.class))
        // .collect(Collectors.toCollection(ArrayList::new));
        result = list.stream().map(movie -> entityToDto(movie)).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(result);
        return result.subList(0, Math.min(num, result.size()));
    }

    // 영화 삭제
    public void deleteMovie(String mid) {
        log.info("영화 삭제");
        movieRepository.deleteById(mid);
    }

    // 영화 수정
    public Movie updateMovie(Movie movie) {
        log.info("영화정보 수정");
        return movieRepository.save(movie);
    }

    public MovieDTO entityToDto(Movie movie) {
        MovieDTO dto = MovieDTO.builder()
                .mid(movie.getMid())
                .movieCd(movie.getMovieCd())
                .title(movie.getTitle())
                .actors(movie.getActors())
                .director(movie.getDirector())
                .openDate(movie.getOpenDate())
                .rank(movie.getRank())
                .genres(movie.getGenres())
                .showTm(movie.getShowTm())
                .nationNm(movie.getNationNm())
                .gradeNm(movie.getGradeNm())
                .synopsis(movie.getSynopsis())
                .imgUrl(movie.getImage().getImgName())
                .replycnt(movie.getReplies().size())
                .followcnt(movie.getFollowcnt())
                .build();
        return dto;
    }
}