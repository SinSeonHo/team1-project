package com.example.ott.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import com.example.ott.entity.Movie;
import com.example.ott.repository.MovieRepository;
import com.example.ott.service.ChatbotService;
import com.example.ott.type.EmotionType;
import com.example.ott.type.GenreType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatbotServiceTest {

    @Mock(lenient = true)
    private MovieRepository movieRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    void testUserEmotionMovie_withRomanceGenre() {
        // given
        Movie romanceMovie = Movie.builder()
                .title("사랑의 블랙홀")
                .genres("로맨스")
                .build();

        when(movieRepository.findByGenresContainingIgnoreCase("로맨스"))
                .thenReturn(List.of(romanceMovie));
        List<Movie> result = chatbotService.userEmtionMoive("행복");
         assertThat(result).isNotEmpty();
        assertThat(result.get(0).getTitle()).contains("사랑");
    }
}
