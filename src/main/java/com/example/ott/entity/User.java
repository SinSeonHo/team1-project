package com.example.ott.entity;

import java.time.LocalDateTime;

import java.util.List;

import org.springframework.data.annotation.CreatedDate;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.ott.type.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@EntityListeners(value = AuditingEntityListener.class)
@Table(name = "user_table")
@Entity
public class User {

    @Id
    private String id;

    @Setter
    private String name;

    @Setter
    @Column(unique = true)
    private String nickname;

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

    @Setter
    @OneToOne(cascade = CascadeType.REMOVE)
    private Image image;

    @OneToMany(mappedBy = "replyer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reply> replies;

}
