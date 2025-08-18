package com.example.ott.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.ott.type.Gender;
import com.example.ott.type.UserRole;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString(exclude = { "replies", "image" })
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_table")
@Entity
public class User {

    @Id
    @Setter
    private String id; // 유저 PK, 아이디

    @Setter
    private String name; // 유저 이름(실명 등)

    @Setter
    @Column(unique = true)
    private String nickname; // 닉네임 (중복 불가)

    @Setter
    @Column(unique = true)
    private String email; // 이메일 (중복 불가)

    @Column(nullable = false)
    private String password; // 비밀번호

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserRole userRole = UserRole.GUEST; // 유저 권한

    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Socials social = Socials.NONE; // 소셜 로그인 종류

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private boolean firstLogin; // 첫 로그인 여부

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate; // 계정 생성일

    @LastModifiedDate
    private LocalDateTime updatedDate; // 마지막 수정일

    @Setter
    private String grade; // 등급 (임의 필드)

    @Setter
    @OneToOne(cascade = CascadeType.REMOVE)
    private Image image; // 프로필 이미지

    @Builder.Default
    private int warningCnt = 0;

    // 유저가 작성한 댓글 리스트 (Reply.replyer와 매핑)
    @OneToMany(mappedBy = "replyer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Reply> replies;

    // 계정 정보 변경 메서드
    public void changeAccountInfo(String id, String password) {
        this.id = id;
        this.password = password;
    }

    public void addWarnningCount(int addCount) {
        this.warningCnt += addCount;
    }
}
