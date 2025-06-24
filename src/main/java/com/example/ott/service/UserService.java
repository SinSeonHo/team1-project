package com.example.ott.service;

import java.util.NoSuchElementException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.User;
import com.example.ott.entity.UserRole;
import com.example.ott.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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

        // 자동 로그인 (반드시 평문 비밀번호 사용!)
        // UsernamePasswordAuthenticationToken authToken = new
        // UsernamePasswordAuthenticationToken(
        // user.getId(),
        // securityUserDTO.getPassword());

        // // 1) 인증 시도
        // Authentication authentication =
        // authenticationManager.authenticate(authToken);

        // // 2) 현재 스레드 컨텍스트에 인증 정보 등록
        // SecurityContextHolder.getContext().setAuthentication(authentication);

        // // 3) 세션에 인증 정보 저장 (다음 요청에도 유지)
        // HttpSession session = request.getSession(true);
        // session.setAttribute(
        // HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        // SecurityContextHolder.getContext());

    }

    // 프로필 조회
    public UserProfileDTO getUserProfile(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 User 정보 입니다."));

        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .mileage(user.getMileage())
                .socials(user.getSocials())
                .build();
    }

    // 프로필 수정
    public void updateUserProfile(UserProfileDTO userProfileDTO) {
        User user = userRepository.findById(userProfileDTO.getId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 User 정보 입니다."));

        user.setNickname(userProfileDTO.getNickname());
        // user.setGenres(userProfileDTO.getGenres());
        userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // 아이디, 비밀번호 변경 (예시)
    public String changeAccountInfo(SecurityUserDTO securityUserDTO) {
        // 암호화된 비밀번호로 체크하려면 matches 사용!
        User user = userRepository.findById(securityUserDTO.getId())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(securityUserDTO.getPassword(), user.getPassword())) {
            return "입력하신 정보가 일치하지 않습니다.";
        }
        // 이미 사용중인 ID 체크
        if (userRepository.existsById(securityUserDTO.getId())) {
            return "이미 존재하는 Id입니다.";
        }
        user.changeAccountInfo(securityUserDTO.getId(), passwordEncoder.encode(securityUserDTO.getPassword()));
        userRepository.save(user);
        return "변경되었습니다";
    }

    // test용 DB상 User 조회
    public User getUser(String id) {
        User user = null;
        try {
            user = userRepository.findById(id).get();
            log.info("검색한 user 내용 : {}", user);
        } catch (NoSuchElementException e) {
            log.info("user 정보를 찾을 수 없음");
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
}
