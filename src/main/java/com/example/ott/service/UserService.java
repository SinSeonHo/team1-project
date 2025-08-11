package com.example.ott.service;

import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.TotalUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.Image;
import com.example.ott.entity.Socials;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;
import com.example.ott.repository.ImageRepository;
import com.example.ott.repository.UserRepository;
import com.example.ott.security.CustomUserDetails;

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
    public String registerAndLogin(TotalUserDTO totalUserDTO) {

        String nickname = (totalUserDTO.getNickname() == null)
                ? makeUniqueNickname(userRepository)
                : totalUserDTO.getNickname();

        Socials social = Socials.NONE;
        UserRole userRole = UserRole.GUEST;
        if (!totalUserDTO.getEmail().isEmpty()) {
            // 소셜 회원가입 일경우 user등급 및 social 종류 추가
                SecurityContext context = SecurityContextHolder.getContext();
                Authentication auth = context.getAuthentication();
                CustomUserDetails cud = (CustomUserDetails) auth.getPrincipal();
                 social = cud.getTemp().getSocial();

            userRole = UserRole.USER;
            
        }

        User user = User.builder()
                .name(totalUserDTO.getName())
                .id(totalUserDTO.getId())
                .nickname(nickname)
                .password(passwordEncoder.encode(totalUserDTO.getPassword()))
                .userRole(userRole)
                .age(totalUserDTO.getAge())
                .gender(totalUserDTO.getGender())
                .email(totalUserDTO.getEmail())
                .social(social)
                .build();

        return userRepository.save(user).getId();
        // social 대기
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
                .profileImageUrl(userProfileUrl)
                .socials(user.getSocial())
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

    // TODO reply 먼저
    // 지워야돼!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // // 아이디, 비밀번호 변경 (예시)
    // public String changeAccountInfo(SecurityUserDTO securityUserDTO) {
    // // 암호화된 비밀번호로 체크하려면 matches 사용!
    // User user = userRepository.findById(securityUserDTO.getId())
    // .orElse(null);

    // if (user == null || !passwordEncoder.matches(securityUserDTO.getPassword(),
    // user.getPassword())) {
    // return "입력하신 정보가 일치하지 않습니다.";
    // }
    // // 이미 사용중인 ID 체크
    // if (userRepository.existsById(securityUserDTO.getId())) {
    // return "이미 존재하는 Id입니다.";
    // }
    // user.changeAccountInfo(securityUserDTO.getId(),
    // passwordEncoder.encode(securityUserDTO.getPassword()));
    // userRepository.save(user);
    // return "변경되었습니다";
    // }

    public User getUserById(String id) {
        User user = null;
        try {
            user = userRepository.findById(id).get();
        } catch (NoSuchElementException e) {
            log.error("user 정보를 찾을 수 없음");
        }
        return user;
    }

    public User getUserByNickname(String nickname) {
        User user = null;
        try {
            user = userRepository.findByNickname(nickname);

        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("해당 유저는 존재하지 않는 유저입니다.");
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
