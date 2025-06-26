package com.example.ott.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChatBotResponseDTO {

    @JsonProperty("message")
    private String response;
}
