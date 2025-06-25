package com.example.ott.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder

@Entity
@Table(name = "user_table")
public class User {

    // TODO : UserCode 생성 기능 추가 필요
    @Id
    private String id;

    private String name; // 실명

    @Setter
    @Column(unique = true)
    private String nickname; // 별명

    @Setter
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.USER;

    @Builder.Default
    private Long mileage = 0L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Setter
    private String grade;

    // private Grade grade? : 마일리지 등급에 따라 레벨 같은 거 꾸며주기(뱃지)

    @Setter
    @OneToOne
    private Image image;

    // @OneToOne(mappedBy = "user")
    // private Favorites favorites;

    public void changeAccountInfo(String id, String password) {
        this.id = id;
        this.password = password;
    }

}
