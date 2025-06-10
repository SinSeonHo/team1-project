package com.example.ott.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Reply {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rid; // 댓글코드

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    // @ManyToOne
    // @JoinColumn(name = "movie_id")
    // private Webtoon webtoon;
}
