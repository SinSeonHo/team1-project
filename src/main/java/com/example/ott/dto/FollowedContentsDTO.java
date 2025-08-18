package com.example.ott.dto;

import com.example.ott.type.ContentsType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowedContentsDTO {
    private String contentsId;
    private String title;
    private String imageUrl;
    private String userId;
    private ContentsType contentsType;
}
