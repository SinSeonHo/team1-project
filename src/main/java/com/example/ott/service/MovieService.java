package com.example.ott.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.MovieDTO;
import com.example.ott.entity.Image;
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

    @Scheduled(cron = "0 0 10 * * *") // 매일 오전10시에 실행
    @Transactional
    public void scheduledMovieImport() {
        log.info("자동 영화 데이터 수집 시작");
        importMovies(); // 기존 메서드 호출
    }

    // 영화 등록
    @Transactional
    public String insertMovie(MovieDTO dto) {
        log.info("영화 등록");

        // 현재 가장 마지막 mid 확인
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
                .build();

        movieRepository.save(movie);

        return movie.getMid();

    }

    // 영화진흥위원회 API 호출 및 insertMovie() 실행하여 DB에 저장
    @Transactional
    public void importMovies() {

        // 어제 날짜 구하기
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // yyyyMMdd 형식으로 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = yesterday.format(formatter);

        // 영화 api 받아오는 url + key + 어제날짜 formattedDate

        String apiUrl1 = "https://kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json"
                + "?key=4cb94726cef5af841db6efd248a5af76"
                + "&targetDt=" + formattedDate;

        try {
            // RestTemplate 외부 API 요청에 사용됨
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl1, String.class);

            // 응답 코드가 200 ~ 299인지 확인한다. 데이터를 정상적으로 받았는지 확인
            if (response.getStatusCode().is2xxSuccessful()) {
                String json = response.getBody();

                // JSON 파싱
                ObjectMapper objectMapper = new ObjectMapper();

                // JSON 문자열을 트리 구조(JsonNode)로 파싱
                JsonNode root = objectMapper.readTree(json);

                // JSON 객체에서 boxOfficeResult 아래 dailyBoxOfficeList 로 내려가서 영화리스트 추출
                JsonNode movieList = root.path("boxOfficeResult").path("dailyBoxOfficeList");

                // JSON 배열 요소들을 하나씩 꺼내 .asText() 문자열로 변환함
                // DB상에 추가적으로 저장해야할 정보가있다면 여기서 변수 선언 후 빌드할것

                for (JsonNode movieNode : movieList) {
                    String movieCd = movieNode.get("movieCd").asText(); // KOBIS 고유 코드
                    String movieNm = movieNode.get("movieNm").asText();
                    String openDt = movieNode.get("openDt").asText();
                    int rank = movieNode.get("rank").asInt();

                    // 영화 상세 정보 요청
                    String apiUrl2 = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/movie/searchMovieInfo.json"
                            + "?key=4cb94726cef5af841db6efd248a5af76"
                            + "&movieCd=" + movieCd;

                    ResponseEntity<String> detailResponse = restTemplate.getForEntity(apiUrl2, String.class);

                    String detailJson = detailResponse.getBody();
                    JsonNode detailRoot = objectMapper.readTree(detailJson);
                    JsonNode movieInfo = detailRoot.path("movieInfoResult").path("movieInfo");

                    // 감독 이름 추출
                    JsonNode directors = movieInfo.path("directors");
                    String directorName = "";
                    if (directors.isArray() && directors.size() > 0) {
                        directorName = directors.get(0).path("peopleNm").asText();

                    } else
                        directorName = "[감독정보없음]";

                    // 배우 이름 전체 문자열로 추출
                    JsonNode actors = movieInfo.path("actors");
                    List<String> actorNames = new ArrayList<>();
                    String actorStr = "";
                    for (JsonNode actorNode : actors) {
                        String actorName = actorNode.path("peopleNm").asText();

                        actorNames.add(actorName);
                    }
                    if (actorNames.isEmpty())
                        actorStr = "[배우정보없음]";
                    else
                        actorStr = String.join(", ", actorNames); // "배우1, 배우2, 배우3..."

                    // 장르 정보 추출
                    JsonNode genres = movieInfo.path("genres");
                    List<String> genreNames = new ArrayList<>();
                    for (JsonNode genreNode : genres) {
                        genreNames.add(genreNode.path("genreNm").asText());
                    }
                    String genreStr = genreNames.isEmpty() ? "[장르정보없음]" : String.join(", ", genreNames);

                    Optional<Movie> optionalMovie = movieRepository.findByMovieCd(movieCd);

                    if (optionalMovie.isPresent()) {
                        // update
                        Movie existing = optionalMovie.get();
                        existing.setRank(rank);
                        existing.setDirector(directorName);
                        existing.setActors(actorStr);
                        existing.setGenres(genreStr);
                        movieRepository.save(existing);
                    } else {

                        // insert
                        String lastId = movieRepository.findLastMovieId(); // mid 생성
                        int nextIdNum = 1;
                        if (lastId != null && lastId.startsWith("m_")) {
                            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
                        }
                        String mid = "m_" + nextIdNum;

                        MovieDTO dto = MovieDTO.builder()
                                .mid("m_" + movieCd)
                                .title(movieNm)
                                .openDate(openDt)
                                .rank(rank)
                                .movieCd(movieCd)
                                .director(directorName)
                                .actors(actorStr)
                                .genres(genreStr)
                                .build();

                        insertMovie(dto);
                    }
                    // JSON 데이터를 그대로 반환
                    // System.out.println(ResponseEntity.ok(movieList));
                    // {"rnum":"1","rank":"1","rankInten":"0","rankOldAndNew":"OLD","movieCd":"20247176","movieNm":"드래곤
                    // 길들이기","openDt":"2025-06-06","salesAmt":"1544927090","salesShare":"35.6","salesInten":"-158473390","salesChange":"-9.3","salesAcc":"5529983300","audiCnt":"151830","audiInten":"-13491","audiChange":"-8.2","audiAcc":"543130","scrnCnt":"1561","showCnt":"5811"}

                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    // 영화 단건 조회
    public Optional<Movie> getMovie(String mid) {
        log.info("영화 한편 조회");
        return movieRepository.findById(mid);
    }

    // 전체 영화 목록 조회
    public List<Movie> getMovieAll() {
        log.info("영화 전체목록 조회");
        return movieRepository.findAll();
    }

    // 영화 삭제
    public void deleteMovie(String mid) {
        log.info("영화 삭제");
        movieRepository.deleteById(mid);
    }

    // 영화 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public Movie updateMovie(Movie movie) {
        log.info("영화정보 수정");
        return movieRepository.save(movie); // ID가 있으면 update
    }

    // ======================================
    // dtoToEntity 및 entityToDto 메소드
    // ======================================
    private Map<String, Object> dtoToEntity(MovieDTO dto) {
        Map<String, Object> resultMap = new HashMap<>();

        Movie movie = Movie.builder()
                .mid(dto.getMid())
                .title(dto.getTitle())
                .build();

        resultMap.put("movie", movie);

        // 이미지 삽입 추후 사용예정
        // List<MovieImageDTO> movieImageDTOs = dto.getMovieImages();
        // if (movieImageDTOs != null && movieImageDTOs.size() > 0) {
        // List<MovieImage> movieImages = movieImageDTOs.stream().map(image -> {
        // MovieImage movieImage = MovieImage.builder()
        // .uuid(image.getUuid())
        // .path(image.getPath())
        // .imgName(image.getImgName())
        // .movie(movie)
        // .build();

        // return movieImage;
        // }).collect(Collectors.toList());
        // resultMap.put("movieImages", movieImages);
        // }
        return resultMap;

    }

    // private MovieDTO entityToDto(Movie movie, List<MovieImage> movieImages, Long
    // count, Double avg) {
    private MovieDTO entityToDto(Movie movie, Long count, Double avg) {

        MovieDTO movieDTO = MovieDTO.builder()
                .mid(movie.getMid())
                .title(movie.getTitle())
                // .createdDate(movie.getCreatedDate())
                .build();

        // 이미지 정보 담기
        // List<MovieImageDTO> mImageDTOs = movieImages.stream().map(movieImage -> {
        // return MovieImageDTO.builder()
        // .inum(movieImage.getInum())
        // .uuid(movieImage.getUuid())
        // .imgName(movieImage.getImgName())
        // .path(movieImage.getPath())
        // .build();
        // }).collect(Collectors.toList());

        // movieDTO.setMovieImages(mImageDTOs);
        movieDTO.setAvg(avg != null ? avg : 0.0);
        movieDTO.setReviewCnt(count);

        return movieDTO;
    }
}