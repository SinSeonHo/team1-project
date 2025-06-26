package com.example.ott.entity;

import java.util.Optional;

import com.example.ott.type.Prohibition;

public class ChatBotBan {
      public static boolean contains(String input) {
        return Prohibition.PROHIBITION.getProhibitionType().stream()
            .anyMatch(ban -> input.toLowerCase().contains(ban.toLowerCase()));
    }

    public static Optional<String> find(String input) {
        return Prohibition.PROHIBITION.getProhibitionType().stream()
            .filter(ban -> input.toLowerCase().contains(ban.toLowerCase()))
            .findFirst();
    }
}
