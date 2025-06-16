package com.example.ott.type;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public enum EmotionType {
    HAPPY("행복",List.of("행복","기뻐","해피엔딩","감동","끝내","자유","시원","재미","재밌는","짜릿","설렘","설레는")
    ,List.of("로맨스", "코미디", "판타지", "모험", "뮤지컬")
),//행복

    EXCITED("흥분",List.of("폭력","전쟁","싸움","긴박")
    ,List.of("액션", "스릴러", "전쟁", "범죄", "어드벤처")),//흥분

    SAD("슬픔",List.of("슬프","우울","뭉클","슬픈","울고","무기력","죽고","외롭","외로운","서글"
    ,"적적","침울","처량"), List.of("드라마", "멜로", "휴먼", "감성", "다큐멘터리")),//슬픔

    ANGRY("분노",List.of("화나", "짜증", "분노", "복수", "폭발", "반항", "불의", "부조리"),
    List.of("범죄", "스릴러", "액션", "드라마")),//분노

    SCARED("공포",List.of("무섭","무서운","갑툭튀","놀람","놀라는","소름돋는","귀신","비명","호러","소름")
    ,List.of("호러", "미스터리", "스릴러", "서스펜스", "판타지")    ),//공포

    BORED("지루함",List.of("지루", "심심", "따분", "할 게", "재미없", "심심해"),
        List.of("코미디", "SF", "판타지", "액션", "어드벤처", "게임"));//지루



    private final String koreanEmotion;
    private final List<String> keyword;
    private final List<String> emotionGenre;

    EmotionType(String koreanEmotion,List<String> keyword,List<String> emotionGenre){
        this.koreanEmotion =koreanEmotion;  
        this.keyword = keyword;
        this.emotionGenre =emotionGenre;
    }
    public String getEmotion(){
        return koreanEmotion;
    }
    public List<String> getKeyword(){
        return keyword;
    }

    public List<String> getEmotionGenre(){
        return emotionGenre;
    }
    public static Optional<EmotionType> emotionKeyword(String input){
        for(EmotionType type : values()){
             for(String keywords : type.keyword){
                if (input.contains(keywords)) {
                    return Optional.of(type);
                }
             }
                    
                
            }
            return Optional.empty();
    }

    public Set<GenreType> genreTypes(){
        return GenreType.enumGenre(String.join(",", this.emotionGenre));
    }
}
