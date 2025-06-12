package com.example.ott.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.GameDTO;
import com.example.ott.dto.MovieDTO;
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

    // 게임 등록
    public String insertGame(GameDTO dto) {
        log.info("게임 등록");

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
                .build();

        gameRepository.save(game);

        return game.getGid();
    }

    // 스팀게임 일일랭킹 top100 API 호출 및 insertGame() 실행하여 DB에 저장
    @Transactional
    public void importGames() {
        String apiUrl1 = "https://steamspy.com/api.php?request=top100owned";

        try {
            // RestTemplate 외부 API 요청에 사용됨
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl1, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String json = response.getBody();

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(json);

                int rank = 1; // 순위는 JSON에 없으므로 반복 순서로 사용

                Iterator<String> fieldNames = root.fieldNames();
                while (fieldNames.hasNext()) {
                    String appid = fieldNames.next(); // 예: "730"
                    JsonNode gameNode = root.path(appid);

                    String name = gameNode.path("name").asText();
                    String developer = gameNode.path("developer").asText();
                    // String publisher = gameNode.path("publisher").asText();
                    // String owners = gameNode.path("owners").asText();
                    // int positive = gameNode.path("positive").asInt();
                    // int negative = gameNode.path("negative").asInt();
                    int ccu = gameNode.path("ccu").asInt();

                    // 게임 상세 정보 요청
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

                    Optional<Game> optionalGame = gameRepository.findByAppid(appid);

                    if (optionalGame.isPresent()) {
                        // ✔ update: rank, price만 수정
                        Game existing = optionalGame.get();
                        existing.setRank(rank);

                        String priceStr = gameNode.path("price").asText(); // 예: "0" 또는 "1999"
                        int price = 0;
                        try {
                            price = Integer.parseInt(priceStr);
                        } catch (NumberFormatException e) {
                            // 가격이 숫자가 아니면 0으로 처리
                        }

                        existing.setPrice(price);
                        gameRepository.save(existing);

                    } else {
                        // ✔ insert: gid, appid, title, developer, rank, ccu, price, platform 저장

                        String lastId = gameRepository.findLastGameId();
                        int nextIdNum = 1;
                        if (lastId != null && lastId.startsWith("g_")) {
                            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
                        }
                        String gid = "g_" + nextIdNum;

                        // 가격 파싱
                        String priceStr = gameNode.path("price").asText();
                        int price = 0;
                        try {
                            price = Integer.parseInt(priceStr);
                        } catch (NumberFormatException e) {
                            // 가격이 숫자가 아니면 0으로 처리
                        }

                        // 플랫폼 정보 (예: "windows", "mac", "linux" 등)
                        JsonNode platformsNode = detailRoot.path(appid).path("data").path("platforms");
                        List<String> platformList = new ArrayList<>();
                        if (platformsNode.path("windows").asBoolean(false))
                            platformList.add("Windows");
                        if (platformsNode.path("mac").asBoolean(false))
                            platformList.add("Mac");
                        if (platformsNode.path("linux").asBoolean(false))
                            platformList.add("Linux");
                        String platform = String.join(", ", platformList); // 예: "Windows, Mac"

                        Game game = Game.builder()
                                .gid(gid)
                                .appid(appid)
                                .title(name)
                                .developer(developer)
                                .rank(rank)
                                .ccu(ccu)
                                .price(price)
                                .platform(platform)
                                .genres(genres)
                                .build();

                        gameRepository.save(game);
                    }

                    rank++; // 다음 게임 순위
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 게임 단건 조회
    public Optional<Game> getGame(String gid) {
        return gameRepository.findById(gid);
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