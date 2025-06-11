package com.example.ott.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.entity.User;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
public class ReplyRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @Test
    void first() {
        Movie movie = Movie.builder().title("title1").build();
        movieRepository.save(movie);
        movie = Movie.builder().title("title2").build();
        movieRepository.save(movie);
    }

    @Test
    void testInsertReply() {
        Movie movie = Movie.builder().mid("").build();
        Reply reply = Reply.builder()
                .replyer(userRepository.findById("user1").get())
                .movie(movie)
                .text("test1")
                .build();
        replyRepository.save(reply);
    }
}
