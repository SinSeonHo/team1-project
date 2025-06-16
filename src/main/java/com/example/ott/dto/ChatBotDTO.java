package com.example.ott.dto;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ToString
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ChatBotDTO {

    private String userId;

    private String text;


}
