package com.example.ott.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ott.dto.ContentsDTO;
import com.example.ott.dto.GameDTO;
import com.example.ott.dto.PageRequestDTO;
import com.example.ott.dto.PageResultDTO;
import com.example.ott.dto.ReplyDTO;
import com.example.ott.entity.Game;
import com.example.ott.repository.GameRepository;
import com.example.ott.type.ContentsType;
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

    private final ContentsService contentsService;

    public List<GameDTO> getTop10() {
        List<Game> games = gameRepository.findTop10ByOrderByRankingAsc();
        List<GameDTO> dtos = games.stream().map(game -> entityToDto(game)).collect(Collectors.toList());

        return dtos;
    }

    @Scheduled(cron = "0 10 10 * * *") // 매일 오전10:10시에 실행
    public void scheduledGameImport() {
        log.info("자동 게임 데이터 수집 시작");
        importGames(); // 기존 메서드 호출
    }

    @Scheduled(cron = "00 15 10 * * *") // 매일 오전10:15에 실행
    public void scheduledGameImageImport() {
        log.info("자동 게임 포스터 반영");
        runPythonGameCrawlerAsync(); // @Async 붙은 메서드 호출
    }

    @CacheEvict(value = "games", key = "'allGames'")
    @Async
    @Transactional
    public void runPythonGameCrawlerAsync() {
        runPythonGameCrawler();
    }

    public void runPythonGameCrawler() {
        try {
            System.out.println("Python 게임 크롤러 실행 시작");

            ProcessBuilder pbImage = new ProcessBuilder("python",
                    "python/gameImageCrwal.py");
            Map<String, String> envImage = pbImage.environment();
            envImage.put("NLS_LANG", "AMERICAN_AMERICA.UTF8");

            Process processImage = pbImage.start();

            // 표준 출력 읽기 (stdout)
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(processImage.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON STDOUT] " + line);
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
                        System.err.println("[PYTHON STDERR] " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            int exitCodeImage = processImage.waitFor();
            System.out.println("게임 이미지 크롤러 종료. Exit code: " + exitCodeImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 게임 등록
    @Transactional
    public String insertGame(GameDTO dto) {
        log.info("게임 저장 시작");

        // 현재 가장 마지막 id확인
        String lastId = gameRepository.findLastGameId();
        int nextIdNum = 1;

        if (lastId != null && lastId.startsWith("g_")) {
            nextIdNum = Integer.parseInt(lastId.substring(2)) + 1;
        }

        String gid = "g_" + nextIdNum;

        Game game = Game.builder()
                .gid(gid)
                .appid(dto.getAppid())
                .title(dto.getTitle())
                .developer(dto.getDeveloper())
                .ccu(dto.getCcu())
                .platform(dto.getPlatform())
                .price(dto.getPrice())
                .ranking(dto.getRanking())
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
        log.info("게임 저장 완료: {}", gid);

        // contents 테이블에 등록
        ContentsDTO contentsDTO = ContentsDTO.builder()
                .contentsId(game.getGid())
                .title(game.getTitle())
                .contentsType(ContentsType.GAME)
                .genres(game.getGenres())
                .build();

        log.info("콘텐츠 저장 시도: {}", contentsDTO);
        contentsService.insertContents(contentsDTO);
        log.info("콘텐츠 저장 완료");

        return game.getGid();
    }

    // 스팀게임 일일랭킹 top100 API 호출 및 insertGame() 실행하여 DB에 저장
    @Transactional
    public void importGames() {
        String apiUrl1 = "https://steamspy.com/api.php?request=top100owned";

        try {
            RestTemplate restTemplate = new RestTemplate();

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

                    String apiUrl2 = "https://store.steampowered.com/api/appdetails?appids=" + appid + "&l=koreana";
                    ResponseEntity<String> detailResponse = restTemplate.getForEntity(apiUrl2, String.class);
                    JsonNode detailRoot = objectMapper.readTree(detailResponse.getBody());
                    JsonNode dataNode = detailRoot.path(appid).path("data");

                    // 장르 처리
                    String genres = "장르정보없음";

                    if (dataNode != null && dataNode.has("genres")) {
                        Set<String> genreSet = new HashSet<>();

                        for (JsonNode genreNode : dataNode.get("genres")) {
                            if (genreNode.has("id")) {
                                int genreId = genreNode.get("id").asInt();

                                // id가 37 or 70이면 건너뜀 (무료 게임 or 얼리 액세스)
                                if (genreId == 37 || genreId == 70) {
                                    continue;
                                }
                            }

                            // description이 존재하면 장르 추가
                            if (genreNode.has("description")) {
                                String engGenre = genreNode.get("description").asText().trim();
                                if (!engGenre.isEmpty()) {
                                    genreSet.add(engGenre);
                                }
                            }
                        }

                        if (!genreSet.isEmpty()) {
                            genres = String.join(", ", genreSet);
                        }
                    }

                    // 가격 처리
                    String originalPrice = "[가격정보없음]";
                    String discountPrice = "[가격정보없음]";
                    int discountRate = 0;

                    if (dataNode != null) {
                        JsonNode priceOverview = dataNode.path("price_overview");

                        boolean isFree = dataNode.path("is_free").asBoolean(false);

                        if (isFree) {
                            discountPrice = "무료플레이";
                            originalPrice = "0";
                            discountRate = 0;
                        } else if (!priceOverview.isMissingNode()) {
                            String tempOriginal = priceOverview.path("initial_formatted").asText("");
                            originalPrice = tempOriginal.isEmpty() ? "0" : tempOriginal;

                            String tempDiscount = priceOverview.path("final_formatted").asText("");
                            discountPrice = tempDiscount.isEmpty() ? "0" : tempDiscount;

                            discountRate = priceOverview.path("discount_percent").asInt(0);
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

                        existing.setRanking(rank);
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
                                .ranking(rank)
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

                        GameDTO dto = entityToDto(game);
                        insertGame(dto);

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
        System.out.println("게임정보 상세조회 로그로그");
        log.info("게임정보 상세조회");

        // 1. 게임 조회
        Game game = gameRepository.findById(gid)
                .orElseThrow(() -> new RuntimeException("게임 없음"));

        // 2. 댓글 DTO 리스트 조회
        List<ReplyDTO> replyDTOList = replyService.contentReplies(gid);

        // 3. Map에 담아서 리턴
        Map<String, Object> result = new HashMap<>();
        result.put("game", game);
        result.put("replies", replyDTOList);

        return result;
    }

    // 전체 게임 목록 조회
    // 최초 호출 시 DB에서 조회 후 캐시에 저장
    @Cacheable(value = "games", key = "'allGames'")
    public List<Game> getGameAll() {
        System.out.println("전체 game 조회 ");
        return gameRepository.findAll();
    }

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
        List<GameDTO> result = new ArrayList<>();
        List<Game> list = getGameAll();

        // 1. 원본 리스트가 비어있다면, 빈 리스트 반환
        if (list.isEmpty()) {
            return null;
        }

        // 2. 'num'과 'list'의 크기 중 더 작은 값을 선택하여 가져올 개수를 결정합니다.
        int countToRetrieve = Math.min(num, list.size());
        int ran = 0;
        List<Integer> eran = new ArrayList<>();
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

    // 게임 삭제
    public void deleteGame(String gid) {
        gameRepository.deleteById(gid);
    }

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
                .negative(game.getNegative())
                .originalPrice(game.getOriginalPrice())
                .platform(game.getPlatform())
                .positive(game.getPositive())
                .price(game.getPrice())
                .publisher(game.getPublisher())
                .ranking(game.getRanking())
                .replycnt(game.getReplies() != null ? game.getReplies().size() : 0)
                .synopsis(game.getSynopsis())
                .followcnt(contentsService.getFollowCnt(game.getGid()))
                .title(game.getTitle())
                .imgUrl((game.getImage() == null) ? null : game.getImage().getImgName())
                .build();
        return dto;
    }
}