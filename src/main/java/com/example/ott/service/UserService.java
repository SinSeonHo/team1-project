package com.example.ott.service;

import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.Image;
import com.example.ott.entity.User;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.UserRepository;
import com.example.ott.type.UserRole;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;

    // 계정 생성 + 자동 로그인
    public String registerAndLogin(SecurityUserDTO securityUserDTO, HttpServletRequest request) {
        User user = User.builder()
                .name(securityUserDTO.getName())
                .id(securityUserDTO.getId())
                .nickname(makeUniqueNickname(userRepository))
                .password(passwordEncoder.encode(securityUserDTO.getPassword()))
                .userRole(UserRole.GUEST)
                .build();

        return userRepository.save(user).getId();

    }

    // 프로필 조회
    public UserProfileDTO getUserProfile(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 User 정보 입니다."));

        String userProfileUrl = (user.getImage() == null ? null : user.getImage().getThumbnailPath());

        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .mileage(user.getMileage())
                .profileImageUrl(userProfileUrl)
                .socials(user.getSocials())
                .grade(user.getUserRole().name())
                .build();
    }

    // 프로필 수정
    public void updateUserProfile(UserProfileDTO userProfileDTO) {
        User user = userRepository.findById(userProfileDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 User 정보 입니다."));

        user.setNickname(userProfileDTO.getNickname());
        user.setName(userProfileDTO.getName());

        userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public User getUser(String id) {
        User user = null;
        try {
            user = userRepository.findById(id).get();
        } catch (NoSuchElementException e) {
            log.error("user 정보를 찾을 수 없음");
        }
        return user;
    }

    // 임시 닉네임 생성
    public String makeUniqueNickname(UserRepository userRepository) {
        String candidate;
        do {
            candidate = "user" + (int) (Math.random() * 100000);
        } while (userRepository.existsByNickname(candidate));
        return candidate;
    }

    public void saveUserProfile(Image profileImage, String id) {
        User user = userRepository.findById(id).get();

        // 기존 이미지가 존재할 시 이미지 삭제
        if (user.getImage() != null) {
            Image currentImage = user.getImage();
            user.setImage(null);
            userRepository.save(user);
            imageRepository.delete(currentImage);
        }
        user.setImage(profileImage);
        userRepository.save(user);
    }

    public void upgradeToAdmin(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));
        user.setUserRole(UserRole.ADMIN);
        userRepository.save(user);
    }
}
