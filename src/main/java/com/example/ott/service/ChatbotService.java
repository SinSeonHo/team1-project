package com.example.ott.service;



import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ChatBotResponseDTO;
import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.type.ContentType;
import com.example.ott.type.EmotionType;
import com.example.ott.type.GenreType;
import com.example.ott.type.Prohibition;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatbotService {

  
    
    private final MovieRepository movieRepository;
    private final GameRepository gameRepository;

    public ChatBotResponseDTO chatBotResponseDTO (String userInput){
        if (prohibitionFind(userInput)) {
            String banWord =findBanWord(userInput).orElse("금지어");
            return new ChatBotResponseDTO("욕설 및 음란 행위가 감지되었습니다 ["+banWord + "]");
        }
        Optional<ContentType> contentType = ContentType.contentType(userInput);
        if (contentType.isEmpty()) {
            return new ChatBotResponseDTO("영화,게임,도서 중 하나만 입력해주세요");
        }
        ContentType type = contentType.get();

        Optional<EmotionType> emOptional = EmotionType.emotionKeyword(userInput);
        if (emOptional.isEmpty()) {
            return new ChatBotResponseDTO("입력하신 말을 이해하지 못했어요");
        }
        Set<GenreType> genreTypes = emOptional.get().genreTypes();
        StringBuilder result = new StringBuilder();
        
        switch (type) {
            case MOVIE:
                List<Movie> movies =findMovieGenre(genreTypes);
                if (movies.isEmpty()) {
                 return new ChatBotResponseDTO("해당 장르에 대한 영화를 찾지 못했어요");
                }
                result.append("추천영화는 : ")
                .append(movies.stream().map(Movie ::getTitle).collect(Collectors.joining(", ")))
                .append("입니다!");
                break;
        
                case GAME :
                List<Game> games = findGameGenre(genreTypes);
                if (games.isEmpty()) {
                    return new ChatBotResponseDTO("해당 장르에 대한 게임을 찾지 못했습니다");
                }
                
                result.append("추천 게임은 :")
                .append(games.stream().map(Game::getTitle).collect(Collectors.joining(", " )))
                .append("입니다!");
                break;
                case WEBTOON :
                return new ChatBotResponseDTO("아직 준비중입니다");
            
        }
        return new ChatBotResponseDTO(result.toString());}
    

    private List<Movie> findMovieGenre(Set<GenreType> genreTypes){
        return genreTypes
        .stream()
        .flatMap(genre -> 
        {log.info("장르 검색중 : {}",genre.getKorean());
        return movieRepository.findByMovieContainingIgnoreCase(genre.getKorean()).stream();
    })
    .distinct().peek(movie -> log.info("추천영화는 : {}",movie.getTitle()))
    .collect(Collectors.toList());
    }
    private List<Game> findGameGenre(Set<GenreType> genreTypes){

        return genreTypes
        .stream()
        .flatMap(genre -> 
        {log.info("장르 검색중 : {}",genre.getKorean());
        return gameRepository.findByGameContainingIgnoreCase(genre.getKorean()).stream();
    })
    .distinct().peek(game -> log.info("추천 게임은 : {}",game.getTitle()))
    .collect(Collectors.toList());
}





   private boolean prohibitionFind(String input){
    String lowerInput = input.toLowerCase();
    return Prohibition.PROHIBITION.getProhibitionType().stream()
    .anyMatch(banned -> lowerInput.contains(banned.toLowerCase()));
   }
   private Optional<String> findBanWord(String input){
    String lowerInput = input.toLowerCase();
    return Prohibition.PROHIBITION.getProhibitionType().stream()
    .filter(banned -> lowerInput.contains(banned.toLowerCase()))
    .findFirst();
   }
}
    
