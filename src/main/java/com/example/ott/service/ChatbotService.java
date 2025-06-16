package com.example.ott.service;



import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.ott.dto.ChatBotResponseDTO;
import com.example.ott.entity.Movie;
import com.example.ott.repository.GameRepository;
import com.example.ott.repository.MovieRepository;
import com.example.ott.repository.WebToonRepository;
import com.example.ott.type.ContentType;
import com.example.ott.type.EmotionType;
import com.example.ott.type.GenreType;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatbotService {
  
    
    private MovieRepository movieRepository;

    public List<Movie> userEmtionMoive (Movie movie,String userEmotion){

        Optional<EmotionType> emotionType = EmotionType.emotionKeyword(userEmotion);
        
        if (emotionType.isEmpty()) {
            return List.of();
        }
       

        EmotionType emotion = emotionType.get();

        Set<GenreType> genreTypes = emotion.genreTypes();

        return genreTypes.stream()
        .map(GenreType :: getKorean)
        .distinct()
        .flatMap(genre -> 
            movieRepository.findByGenresContainingIgnoreCase(genre)
            .stream())
            .distinct()
            .toList();
        
        




      


      





      
    }
}
