package com.example.ott.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Contents;
import com.example.ott.entity.ContentsType;
import com.example.ott.entity.Movie;
import com.example.ott.repository.ContentsRepository;
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
    private final ContentsService contentsService;

    @Scheduled(cron = "00 00 10 * * *") // 매일 오전10:00에 실행
    public void scheduledMovieImport() {
        log.info("자동 영화 데이터 수집 시작");
        importMovies();
    }

    @Scheduled(cron = "00 03 10 * * *") // 매일 오전10:03에 실행
    public void scheduledMovieSynopsisImport() {
        log.info("자동 영화 줄거리 및 포스터 반영");
        runPythonMovieCrawlerAsync();
    }

    // 영화 추가/수정 시 캐시 무효화
    @CacheEvict(value = "movies", key = "'allMovies'")
    @Async
    @Transactional
    public void runPythonMovieCrawlerAsync() {
        runPythonMovieCrawler();
    }

    public void runPythonMovieCrawler() {
        try {
            System.out.println("Python 영화 크롤러 실행 시작");

            // 첫 번째 파이썬 스크립트 실행 (영화 줄거리 크롤러)
            ProcessBuilder pbSynopsis = new ProcessBuilder("python",
                    "python/movieSynopsisCrwal.py");
            Map<String, String> env = pbSynopsis.environment();
            env.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");
            Process processSynopsis = pbSynopsis.start();

            // 표준 출력 읽기 (stdout)
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processSynopsis.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON SYNOPSIS STDOUT] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 에러 출력 읽기 (stderr)
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processSynopsis.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("[PYTHON SYNOPSIS STDERR] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            int exitCodeSynopsis = processSynopsis.waitFor();
            System.out.println("영화 줄거리 크롤러 종료. Exit code: " + exitCodeSynopsis);

            if (exitCodeSynopsis == 0) {
                // 두 번째 파이썬 스크립트 실행 (영화 이미지 크롤러)
                System.out.println("Python 영화 포스터 크롤링 시작");
                ProcessBuilder pbImage = new ProcessBuilder("python",
                        "python/movieImageCrwal.py");
                Map<String, String> envImage = pbImage.environment();
                envImage.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");
                Process processImage = pbImage.start();

                // 표준 출력 읽기 (stdout)
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(processImage.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[PYTHON IMAGE STDOUT] " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                // 에러 출력 읽기 (stderr)
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(processImage.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println("[PYTHON IMAGE STDERR] " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();

                int exitCodeImage = processImage.waitFor();
                System.out.println("영화 이미지 크롤러 종료. Exit code: " + exitCodeImage);
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
                .ranking(dto.getRanking())
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

        // contents 테이블에 등록
        ContentsDTO contentsDTO = ContentsDTO.builder()
                .contentsId(movie.getMid())
                .title(movie.getTitle())
                .contentsType(ContentsType.MOVIE)
                .genres(movie.getGenres())
                .build();
        contentsService.insertContents(contentsDTO);

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
                        existing.setRanking(rank);
                        existing.setDirector(directorName);
                        existing.setActors(actorStr);
                        existing.setGenres(genreStr);
                        movieRepository.save(existing);
                    } else {
                        MovieDTO dto = MovieDTO.builder()
                                .mid("m_" + movieCd)
                                .title(movieNm)
                                .openDate(openDt)
                                .ranking(rank)
                                .movieCd(movieCd)
                                .director(directorName)
                                .actors(actorStr)
                                .genres(genreStr)
                                .showTm(convertShowTm(showTm))
                                .nationNm(nationNm)
                                .gradeNm(gradeNm)
                                .synopsis(null) // 초기 줄거리 없음
                                .build();

                        insertMovie(dto);
                    }
                    rank++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 영화 단건 상세정보 + 댓글 리스트 조회
    public Map<String, Object> getMovie(String mid) {
        log.info("영화정보 상세조회");

        Movie movie = movieRepository.findById(mid)
                .orElseThrow(() -> new RuntimeException("영화 없음"));

        List<ReplyDTO> replyDTOList = replyService.contentReplies(mid);

        Map<String, Object> result = new HashMap<>();
        result.put("movie", movie);
        result.put("replies", replyDTOList);

        return result;
    }

    public PageResultDTO<MovieDTO> getSearch(PageRequestDTO requestDTO) {
        Page<Movie> result = movieRepository.search(requestDTO);

        List<MovieDTO> dtoList = result.stream()
                .map(movie -> entityToDto(movie))
                .collect(Collectors.toList());

        return PageResultDTO.<MovieDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(requestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    // 전체 영화 목록 조회
    // 최초 호출 시 DB에서 조회 후 캐시에 저장
    @Cacheable(value = "movies", key = "'allMovies'")
    public List<Movie> getMovieAll() {
        log.info("영화 전체목록 조회");
        return movieRepository.findAll();
    }

    public List<MovieDTO> getRandom(int num) {
        List<MovieDTO> result = new ArrayList<>();
        List<Movie> list = getMovieAll(); // 1. 원본 리스트가 비어있다면, 빈 리스트 반환
        if (list.isEmpty()) {
            return null;
        }

        // 2. 'num'과 'list'의 크기 중 더 작은 값을 선택하여 가져올 개수를 결정합니다.
        int countToRetrieve = Math.min(num, list.size());
        int ran = 0;
        Set<Integer> eran = new HashSet<>();

        // 3. countToRetrieve 크기만큼
        while (countToRetrieve > eran.size()) {
            ran = (int) (Math.random() * list.size());
            eran.add(ran);
        }
        // 4. 결정된 개수만큼 앞에서부터 요소를 가져와 DTO로 변환하여 결과 리스트에 추가합니다.
        for (Integer r : eran) {
            result.add(entityToDto(list.get(r)));
        }
        return result;
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

    // 상영시간을 n시간 n분형태로 변환하여 반환
    private String convertShowTm(Integer minutes) {
        if (minutes == null || minutes == 0)
            return "상영시간없음";
        int hrs = minutes / 60;
        int mins = minutes % 60;
        return hrs + "시간 " + mins + "분";
    }

    public MovieDTO entityToDto(Movie movie) {

        MovieDTO dto = MovieDTO.builder()
                .mid(movie.getMid())
                .movieCd(movie.getMovieCd())
                .title(movie.getTitle())
                .actors(movie.getActors())
                .director(movie.getDirector())
                .openDate(movie.getOpenDate())
                .ranking(movie.getRanking())
                .genres(movie.getGenres())
                .showTm(movie.getShowTm())
                .nationNm(movie.getNationNm())
                .gradeNm(movie.getGradeNm())
                .synopsis(movie.getSynopsis())
                .imgUrl((movie.getImage() == null) ? null : movie.getImage().getImgName())
                .replycnt(movie.getReplies() != null ? movie.getReplies().size() : 0)
                .followcnt(contentsService.getFollowCnt(movie.getMid()))
                .build();

        if (movie.getImage() == null) {
            dto.setImgUrl("");
        } else {
            dto.setImgUrl(movie.getImage().getImgName());
        }
        return dto;
    }
}