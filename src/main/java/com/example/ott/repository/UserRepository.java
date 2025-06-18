package com.example.ott.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ott.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);

    User findByIdAndPassword(String id, String password);

    boolean existsById(String id);
}
