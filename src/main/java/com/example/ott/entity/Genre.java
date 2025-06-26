package com.example.ott.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Genre {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String gen_id; // 장르코드
    // private String gen_name;

    // @ManyToOne
    // @JoinColumn(name = "movie_id")
    // private Movie movie;

    // @ManyToOne
    // @JoinColumn(name = "game_id")
    // private Game game;

    // @ManyToOne
    // @JoinColumn(name = "webtoon_id")
    // private Webtoon webtoon;

}
