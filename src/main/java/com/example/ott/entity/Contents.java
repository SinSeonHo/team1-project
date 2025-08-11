package com.example.ott.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "movie", "game" })

@Entity
public class Contents {

    @Id
    private String contentsId;

    @Setter
    @Enumerated(EnumType.STRING)
    private ContentsType contentsType;

    private String title;

    // contents Ids
    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mid", unique = true)
    private Movie movie;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gid", unique = true)
    private Game game;

    @Builder.Default
    private int followCnt = 0;

    public void addFollowCnt() {
        this.followCnt += 1;
    }

    public void minusFollowCnt() {
        if (this.followCnt == 0) {
            return;
        }
        this.followCnt -= 1;
    }
}
