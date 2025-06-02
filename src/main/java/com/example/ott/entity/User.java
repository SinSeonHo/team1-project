package com.example.ott.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

@Entity
public class User {
    
    // TODO : UserCode 생성 기능 추가 필요
    @Id
    private String userCode;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private Long mileage = 0L;

    // private Struct struct;
}
