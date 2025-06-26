package com.example.ott.type;

import java.util.List;
import java.util.Optional;

public enum Prohibition {
    PROHIBITION("욕",List.of("시발","병신","닥쳐","섹스"));



    private final List<String> prohibitionType;
    private final String ban;

    Prohibition(String ban,List<String> prohibitionType){
        this.prohibitionType = prohibitionType;
        this.ban = ban;
    }
    public List<String> getProhibitionType(){
        return prohibitionType;
    }
    public String getBan(){
        return ban;
    }


    
}
