package com.example.ott.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.Socials;
import com.example.ott.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    // User findByEmail(String email);

    User findByEmail(String email);

    User findByNickname(String nickname);

    User findByIdAndPassword(String id, String password);

    boolean existsById(String id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, String id);

    // Optional<User> findByUser(User user);

}
