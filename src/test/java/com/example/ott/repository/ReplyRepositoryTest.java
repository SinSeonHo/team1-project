package com.example.ott.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.entity.User;
import com.example.ott.entity.WebToon;

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
                Movie movie = Movie.builder().mid("m_1").title("title1").build();
                movieRepository.save(movie);
                movie = Movie.builder().mid("m_2").title("title2").build();
                movieRepository.save(movie);
        }

        @Test
        void testInsertReply() {

                Movie movie = Movie.builder().mid("m_1").build();
                Game game = Game.builder().gid("g_1").build();
                WebToon webtoon = WebToon.builder().wid(1L).build();
                Reply reply = Reply.builder()
                                .replyer(userRepository.findById("user2").get())
                                .movie(movie)
                                .game(null)
                                // .webtoon(null)
                                .text("test3")
                                .build();
                replyRepository.save(reply);
        }

        @Test
        void testInsertReReply() {
                Movie movie = Movie.builder().mid("m_1").build();
                Reply reply = Reply.builder()
                                .replyer(User.builder().id("user1").build())
                                .movie(movie)
                                .game(null)
                                // .webtoon(null)
                                .text("test rereply")
                                .ref(11l)
                                .mention("user1")
                                .build();
                replyRepository.save(reply);
        }
}
