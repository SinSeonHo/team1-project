package com.example.ott.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product") // 실제 DB 테이블명이 한글인 경우
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product-num")
    private Long id;

    @Column(name = "badge-name")
    private String badgeName;

    // 기본 생성자
    public Product() {
    }

    // 생성자
    public Product(String badgeName) {
        this.badgeName = badgeName;
    }

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }
}
