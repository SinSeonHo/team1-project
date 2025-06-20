package com.example.ott.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.GameRepository;
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

    @Scheduled(cron = "0 01 10 * * *") // 매일 오전10시에 실행
    @Transactional
    public void scheduledGameImport() {
        log.info("자동 게임 데이터 수집 시작");
        importGames(); // 기존 메서드 호출
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
                .reviewSummary(dto.getReviewSummary())
                .build();

        gameRepository.save(game);

        return game.getGid();
    }

    // 스팀게임 일일랭킹 top100 API 호출 및 insertGame() 실행하여 DB에 저장
    @Transactional
    public void importGames() {
        String apiUrl1 = "https://steamspy.com/api.php?request=top100owned";

        try {
            RestTemplate restTemplate = new RestTemplate();
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

                    String genres = "[장르정보없음]";
                    if (dataNode != null && dataNode.has("genres")) {
                        List<String> genreList = new ArrayList<>();
                        for (JsonNode genreNode : dataNode.get("genres")) {
                            genreList.add(genreNode.get("description").asText());
                        }
                        genres = String.join(", ", genreList);
                    }

                    int originalPrice = 0;
                    int discountRate = 0;
                    int discountPrice = 0;

                    if (dataNode != null) {
                        JsonNode priceOverview = dataNode.path("price_overview");
                        if (!priceOverview.isMissingNode()) {
                            originalPrice = priceOverview.path("initial").asInt(0);
                            discountPrice = priceOverview.path("final").asInt(0);
                            discountRate = priceOverview.path("discount_percent").asInt(0);
                        }
                    }

                    String publisher = "[배급사정보없음]";
                    if (dataNode != null) {
                        JsonNode publishersNode = dataNode.path("publishers");
                        if (publishersNode.isArray() && publishersNode.size() > 0) {
                            publisher = publishersNode.get(0).asText();
                        }
                    }

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

                    // 새로 추가한 리뷰 관련 필드 초기화
                    int positive = 0;
                    int negative = 0;
                    String reviewSummary = "[평론정보없음]";

                    // SteamSpy API에 긍정/부정 평가 정보 있음
                    positive = gameNode.path("positive").asInt(0);
                    negative = gameNode.path("negative").asInt(0);

                    // Steam Storefront API의 review_summary 정보도 받아오기
                    if (dataNode != null) {
                        JsonNode reviewNode = dataNode.path("reviews");
                        if (!reviewNode.isMissingNode()) {
                            reviewSummary = reviewNode.path("review_score_desc").asText("[평론정보없음]");
                        }
                    }

                    Optional<Game> optionalGame = gameRepository.findByAppid(appid);

                    if (optionalGame.isPresent()) {
                        Game existing = optionalGame.get();

                        existing.setRank(rank);
                        existing.setOriginalPrice(originalPrice / 100);
                        existing.setPrice(discountPrice / 100);
                        existing.setDiscountRate(discountRate);
                        existing.setPublisher(publisher);
                        existing.setAgeRating(ageRating);

                        // 새 필드 업데이트
                        existing.setPositive(positive);
                        existing.setNegative(negative);
                        existing.setReviewSummary(reviewSummary);

                        gameRepository.save(existing);

                    } else {
                        String lastId = gameRepository.findLastGameId();
                        int nextIdNum = 1;
                        if (lastId != null && lastId.startsWith("g_")) {
                            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
                        }
                        String gid = "g_" + nextIdNum;

                        JsonNode platformsNode = dataNode.path("platforms");
                        List<String> platformList = new ArrayList<>();
                        if (platformsNode.path("windows").asBoolean(false))
                            platformList.add("Windows");
                        if (platformsNode.path("mac").asBoolean(false))
                            platformList.add("Mac");
                        if (platformsNode.path("linux").asBoolean(false))
                            platformList.add("Linux");
                        String platform = String.join(", ", platformList);

                        Game game = Game.builder()
                                .gid(gid)
                                .appid(appid)
                                .title(name)
                                .developer(developer)
                                .rank(rank)
                                .ccu(ccu)
                                .originalPrice(originalPrice / 100)
                                .price(discountPrice / 100)
                                .discountRate(discountRate)
                                .platform(platform)
                                .genres(genres)
                                .publisher(publisher)
                                .ageRating(ageRating)
                                // 새 필드 포함
                                .positive(positive)
                                .negative(negative)
                                .reviewSummary(reviewSummary)
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

    // 전체 게임 목록 조회
    public List<Game> getGameAll() {
        return gameRepository.findAll();
    }

    // 게임 삭제
    public void deleteGame(String gid) {
        gameRepository.deleteById(gid);
    }

    // 게임 수정 MANAGER, ADMIN만 수정 가능하도록 할 예정
    public Game updateGame(Game game) {
        return gameRepository.save(game); // ID가 있으면 update
    }
}