package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ott.entity.Game;
import com.example.ott.entity.Movie;
import com.example.ott.entity.Reply;
import com.example.ott.entity.Report;
import com.example.ott.entity.User;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    List<Reply> findByRef(Long ref);

    @Query("select r from Reply r where r.movie = :movie")
    List<Reply> findByMovie(Movie movie);

    // @Query("select r from Reply r where r.game = :game")
    // List<Reply> findByGame(Game Game);
    // 0619 신선호 임의로 수정함
    @Query("select r from Reply r where r.game = :game")
    List<Reply> findByGame(@org.springframework.data.repository.query.Param("game") Game game);

    Optional<Reply> findByReplyerAndMovieAndRefIsNull(User replyer, Movie movie);

    Optional<Reply> findByReplyerAndGameAndRefIsNull(User replyer, Game game);

}