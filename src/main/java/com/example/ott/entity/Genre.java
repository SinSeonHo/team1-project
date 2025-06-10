package com.example.ott.entity;

import com.example.ott.type.ContentType;
import com.example.ott.type.genretype;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eid;


    @Enumerated(EnumType.STRING)
    private ContentType contentType;


    @Enumerated(EnumType.STRING)
    private genretype genretypes;


    
}
