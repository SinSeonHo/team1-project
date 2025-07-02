package com.example.ott.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.GameRepository;
import com.example.ott.type.GenreType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final ReplyService replyService;
    private final ModelMapper modelMapper;

    @Scheduled(cron = "0 02 10 * * *") // 매일 오전10시에 실행
    @Transactional
    public void scheduledGameImport() {
        log.info("자동 게임 데이터 수집 시작");
        importGames(); // 기존 메서드 호출
    }

    @Scheduled(cron = "10 46 11 * * *") // 매일 오전10:01에 실행
    @Transactional
    public void scheduledGameImageImport() {
        log.info("자동 게임 포스터 반영");
        runPythonGameCrawler();
    }

    public void runPythonGameCrawler() {
        try {
            System.out.println("Python 게임 크롤러 실행 시작");

            // 파이썬 스크립트 실행 (게임 이미지 크롤러)
            ProcessBuilder pbImage = new ProcessBuilder("python",
                    "C:/SOURCE/team1-project/python/gameImageCrwal.py");
            Map<String, String> envImage = pbImage.environment();
            envImage.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");
            Process processImage = pbImage.start();
            int exitCodeImage = processImage.waitFor();
            System.out.println("게임 이미지 크롤러 종료. Exit code: " + exitCodeImage);

        } catch (Exception e) {
            System.err.println("파이썬 실행 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 게임 등록
    public String insertGame(GameDTO dto) {
        log.info("db에 게임 저장");

        // 현재 가장 마지막 gid 확인
        String lastId = gameRepository.findLastGameId();
        int nextIdNum = 1;

        if (lastId != null && lastId.startsWith("g_")) {
            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
        }

        String gid = "g_" + nextIdNum;

        Game game = Game.builder()
                .gid(gid)
                .title(dto.getTitle())
                .developer(dto.getDeveloper())
                .ccu(dto.getCcu())
                .platform(dto.getPlatform())
                .price(dto.getPrice())
                .rank(dto.getRank())
                .genres(dto.getGenres())
                .originalPrice(dto.getOriginalPrice())
                .discountRate(dto.getDiscountRate())
                .publisher(dto.getPublisher())
                .ageRating(dto.getAgeRating())
                .positive(dto.getPositive())
                .negative(dto.getNegative())
                .synopsis(dto.getSynopsis())
                .build();

        gameRepository.save(game);

        return game.getGid();
    }

    // 스팀게임 일일랭킹 top100 API 호출 및 insertGame() 실행하여 DB에 저장
    @Transactional
    public void importGames() {
        String apiUrl1 = "https://steamspy.com/api.php?request=top100owned";

        double krwToUsdRate = 1300.0; // 초기값 (예비용)

        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. 환율 API 호출 (KRW -> USD)
            String rateApiUrl = "https://api.exchangerate.host/latest?base=KRW&symbols=USD";
            ResponseEntity<String> rateResponse = restTemplate.getForEntity(rateApiUrl, String.class);
            if (rateResponse.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rateRoot = mapper.readTree(rateResponse.getBody());
                krwToUsdRate = rateRoot.path("rates").path("USD").asDouble(krwToUsdRate);
            }

            // 2. SteamSpy API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl1, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String json = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(json);

                int rank = 1;

                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    String appid = fieldNames.next();
                    JsonNode gameNode = root.path(appid);

                    String name = gameNode.path("name").asText();
                    String developer = gameNode.path("developer").asText();
                    int ccu = gameNode.path("ccu").asInt();

                    String apiUrl2 = "https://store.steampowered.com/api/appdetails?appids=" + appid;
                    ResponseEntity<String> detailResponse = restTemplate.getForEntity(apiUrl2, String.class);
                    JsonNode detailRoot = objectMapper.readTree(detailResponse.getBody());
                    JsonNode dataNode = detailRoot.path(appid).path("data");

                    // 장르 처리
                    String genres = "장르정보없음";
                    if (dataNode != null && dataNode.has("genres")) {
                        Set<String> genreSet = new HashSet<>();

                        for (JsonNode genreNode : dataNode.get("genres")) {
                            String engGenre = genreNode.get("description").asText();
                            String korGenre = GenreType.toKorean(engGenre);
                            genreSet.add(korGenre);
                        }

                        // 장르정보없음이 여러 번 들어가지 않도록
                        if (genreSet.size() == 1 && genreSet.contains("장르정보없음")) {
                            genres = "장르정보없음";
                        } else {
                            genreSet.remove("장르정보없음"); // 중복 제거
                            genres = String.join(", ", genreSet);
                        }
                    }

                    // 가격 처리
                    int originalPrice = 0;
                    int discountRate = 0;
                    int discountPrice = 0;

                    if (dataNode != null) {
                        JsonNode priceOverview = dataNode.path("price_overview");
                        if (!priceOverview.isMissingNode()) {
                            String currency = priceOverview.path("currency").asText("USD");
                            int initial = priceOverview.path("initial").asInt(0);
                            int finalPrice = priceOverview.path("final").asInt(0);
                            discountRate = priceOverview.path("discount_percent").asInt(0);

                            if ("KRW".equalsIgnoreCase(currency)) {
                                originalPrice = (int) Math.round(initial / krwToUsdRate);
                                discountPrice = (int) Math.round(finalPrice / krwToUsdRate);
                            } else {
                                originalPrice = initial / 100;
                                discountPrice = finalPrice / 100;
                            }
                        }
                    }

                    // 배급사
                    String publisher = "[배급사정보없음]";
                    if (dataNode != null) {
                        JsonNode publishersNode = dataNode.path("publishers");
                        if (publishersNode.isArray() && publishersNode.size() > 0) {
                            publisher = publishersNode.get(0).asText();
                        }
                    }

                    // 이용연령
                    String ageRating = "[이용연령정보없음]";
                    if (dataNode != null) {
                        int requiredAge = dataNode.path("required_age").asInt(0);
                        if (requiredAge > 0) {
                            ageRating = requiredAge + "세 이상";
                        } else {
                            JsonNode contentDesc = dataNode.path("content_descriptors").path("ids");
                            if (!contentDesc.isMissingNode() && contentDesc.isArray() && contentDesc.size() > 0) {
                                ageRating = "청소년 이용불가";
                            }
                        }
                    }

                    // 긍정/부정 평가
                    int positive = gameNode.path("positive").asInt(0);
                    int negative = gameNode.path("negative").asInt(0);

                    // 플랫폼 정보
                    JsonNode platformsNode = dataNode.path("platforms");
                    List<String> platformList = new ArrayList<>();
                    if (platformsNode.path("windows").asBoolean(false))
                        platformList.add("Windows");
                    if (platformsNode.path("mac").asBoolean(false))
                        platformList.add("Mac");
                    if (platformsNode.path("linux").asBoolean(false))
                        platformList.add("Linux");
                    String platform = String.join(", ", platformList);

                    // DB 저장 또는 업데이트
                    Optional<Game> optionalGame = gameRepository.findByAppid(appid);

                    if (optionalGame.isPresent()) {
                        Game existing = optionalGame.get();

                        existing.setRank(rank);
                        existing.setOriginalPrice(originalPrice);
                        existing.setPrice(discountPrice);
                        existing.setDiscountRate(discountRate);
                        existing.setPublisher(publisher);
                        existing.setAgeRating(ageRating);
                        existing.setPositive(positive);
                        existing.setNegative(negative);

                        gameRepository.save(existing);
                    } else {
                        String lastId = gameRepository.findLastGameId();
                        int nextIdNum = 1;
                        if (lastId != null && lastId.startsWith("g_")) {
                            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
                        }
                        String gid = "g_" + nextIdNum;

                        Game game = Game.builder()
                                .gid(gid)
                                .appid(appid)
                                .title(name)
                                .developer(developer)
                                .rank(rank)
                                .ccu(ccu)
                                .originalPrice(originalPrice)
                                .price(discountPrice)
                                .discountRate(discountRate)
                                .platform(platform)
                                .genres(genres)
                                .publisher(publisher)
                                .ageRating(ageRating)
                                .positive(positive)
                                .negative(negative)
                                .build();

                        gameRepository.save(game);
                    }

                    rank++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 게임단건 상세정보 + 해당 게임 댓글리스트 조회
    // 게임 + 댓글 DTO 리스트 함께 반환

    public Map<String, Object> getGame(String gid) {
        log.info("영화정보 상세조회");

        // 1. 게임 조회
        Game game = gameRepository.findById(gid)
                .orElseThrow(() -> new RuntimeException("영화 없음"));

        // 2. 댓글 DTO 리스트 조회
        List<ReplyDTO> replyDTOList = replyService.gameReplies(gid);

        // 3. Map에 담아서 리턴
        Map<String, Object> result = new HashMap<>();
        result.put("game", game);
        result.put("replies", replyDTOList);

        return result;
    }

    // 인기 게임 목록 조회
    public List<Game> getGameRank(int num) {
        List<Game> list = gameRepository.findAll(Sort.by("rank"));
        List<Game> result;
        // originalList에 10개 이상 있으면 0~9까지 자르고, 아니면 전부 복사
        if (list.size() > num) {
            result = new ArrayList<>(list.subList(0, num));
        } else {
            result = new ArrayList<>(list);
        }
        return result;
    }

    // 전체 게임 목록 조회
    public List<Game> getGameAll() {
        return gameRepository.findAll();
    }
    // 주석처리 6/25
    // public PageResultDTO<GameDTO> getGameRequest(PageRequestDTO requestDTO) {
    // Page<Game> result = gameRepository.search(requestDTO);

    // List<GameDTO> dtoList = result.stream()
    // .map(game -> entityToDto(game))
    // .collect(Collectors.toList());

    // return PageResultDTO.<GameDTO>withAll()
    // .dtoList(dtoList)
    // .pageRequestDTO(requestDTO)
    // .totalCount(result.getTotalElements())
    // .build();
    // }

    public PageResultDTO<GameDTO> getSearch(PageRequestDTO requestDTO) {
        Page<Game> result = gameRepository.search(requestDTO);

        List<GameDTO> dtoList = result.stream()
                .map(game -> entityToDto(game))
                .collect(Collectors.toList());

        return PageResultDTO.<GameDTO>withAll()
                .dtoList(dtoList)
                .pageRequestDTO(requestDTO)
                .totalCount(result.getTotalElements())
                .build();
    }

    public List<GameDTO> getRandom(int num) {
        List<GameDTO> result;
        List<Game> list = gameRepository.findAll();
        result = list.stream().map(game -> entityToDto(game)).collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(result);
        return result.subList(0, Math.min(num, result.size()));
    }

    // 게임 삭제
    public void deleteGame(String gid) {
        gameRepository.deleteById(gid);
    }

    // 게임 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public Game updateGame(Game game) {
        return gameRepository.save(game); // ID가 있으면 update
    }

    public GameDTO entityToDto(Game game) {
        GameDTO dto = GameDTO.builder()
                .ageRating(game.getAgeRating())
                .appid(game.getAppid())
                .ccu(game.getCcu())
                .developer(game.getDeveloper())
                .discountRate(game.getDiscountRate())
                .genres(game.getGenres())
                .gid(game.getGid())
                .imgUrl(game.getImage().getImgName())
                .negative(game.getNegative())
                .originalPrice(game.getOriginalPrice())
                .platform(game.getPlatform())
                .positive(game.getPositive())
                .price(game.getPrice())
                .publisher(game.getPublisher())
                .rank(game.getRank())
                .replycnt(game.getReplies().size())
                .synopsis(game.getSynopsis())
                .followcnt(game.getFollowcnt())
                .title(game.getTitle())
                .build();
        return dto;
    }
}