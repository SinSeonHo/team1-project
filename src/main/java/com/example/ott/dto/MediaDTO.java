package com.example.ott.dto;

public class MediaDTO {

    private String url;
    private String type; // "image" 또는 "video"

    public MediaDTO(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
