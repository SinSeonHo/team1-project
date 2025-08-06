package com.example.ott.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 배지 ID
    private Image img; // 이미지 엔티티와 연관 관계 설정
    private String bgName; // 배지 이름
    private int ord;

}
