package com.example.ott.dto;

import java.util.Arrays;
import java.util.List;

import com.example.ott.type.ContentsType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class ContentsDTO {

    private String contentsId;
    private String title;
    private ContentsType contentsType;
    private List<String> genreNames;

    private String imgUrl;
    private int followCnt;
    private int replyCnt;
    private int ranking;

    public static class ContentsDTOBuilder {
        public ContentsDTOBuilder genres(String genres) {

            this.genreNames = Arrays.stream(genres.split(","))
                    .map(String::trim)
                    .toList();
            return this;
        }
    }
}
