package com.example.ott.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountDatasDTO {
    private long contentsCnt;
    private long userCnt;
    private long followedCnt;
    private long replyCnt;
}
