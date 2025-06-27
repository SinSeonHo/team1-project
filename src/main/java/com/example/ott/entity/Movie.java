package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class Movie extends BaseEntity {

    @Id

    private String mid;

    @Column(nullable = false)
    private String title;
    private String openDate;

    @Column(nullable = false)
    private int rank;

    @Column(unique = true)
    private String movieCd;

    private String director;
    @Column(length = 1000)
    private String actors;

    private String genres;
    private int showTm;
    private String nationNm;
    private String gradeNm;
    @Column(length = 10000)
    private String synopsis;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST)
    private List<Reply> replies = new ArrayList<>();

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "image_id", nullable = true)
    private Image image;

    @Builder.Default
    private int followcnt = 0;

    public void setFollowcnt(int followcnt) {
        this.followcnt = followcnt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOpenDate(String openDate) {
        this.openDate = openDate;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}