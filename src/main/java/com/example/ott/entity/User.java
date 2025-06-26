package com.example.ott.entity;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.data.annotation.CreatedDate;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder

@EntityListeners(value = AuditingEntityListener.class)
@Table(name = "user_table")
@Entity
public class User {

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
    private UserRole userRole = UserRole.GUEST;

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Socials socials = Socials.NONE; // 소셜 계정(Kakao, Naver, Google, X)

    @Builder.Default
    @Setter
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
