package com.example.ott.type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ContentType {
    MOVIE("영화",List.of("무비")),
    WEBTOON("도서",List.of("만화","책","웹툰")),
    GAME("게임",List.of("겜","오락"));

    private final String korean;
    private final List<String> contentKorean;

    ContentType(String korean,List<String> contentKorean){
        this.korean= korean;
        this.contentKorean= contentKorean;
    }
    public String getKorean(){
        return korean;
    }
    public static Optional<ContentType> contentType(String input){
        String lowerInput = input.toLowerCase();
        for(ContentType type : values()){
            if (lowerInput.contains(type.korean.toLowerCase())) {
                return Optional.of(type);
                 }
                 for(String keyword : type.contentKorean){
                    if (lowerInput.contains(keyword.toLowerCase())) {
                        return Optional.of(type);
                    }
                    
                }
        }return Optional.empty();
    
    }
}
