package com.example.ott.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product-num")
    private Long id;

    @Column(name = "badge-name")
    private String badgeName;

    @Column(name = "type") // 🔹 타입 컬럼 추가
    private String type;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

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
