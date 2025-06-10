package com.example.ott.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Genre {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String gid; // 장르코드

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    // @ManyToOne
    // @JoinColumn(name = "webtoon_id")
    // private Webtoon webtoon;

}
