package com.example.ott.service;

import org.springframework.stereotype.Service;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.User;
import com.example.ott.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    // 계정 생성
    // 계정 생성 접근 시 이전 유형은 소셜 인증 여부 = 소셜로 
    // 회원가입 폼에선 아이디, 
    // 일단 아이디, 비밀번호, 소셜 로그인 공간을 생성
    // 그 다음에 프로필 설정으로 넘어옴

    // 계정 생성
    public void register(SecurityUserDTO securityUserDTO) {
        User user = User.builder()
        .id(securityUserDTO.getId())
        .password(securityUserDTO.getPassword())
        .build();

        userRepository.save(user);
    }

    // 프로필 조회
    public UserProfileDTO getUserProfile(String id) {
        
        User user = userRepository.findById(id).get();
        
        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
        .email(user.getEmail())
        .name(user.getName())
        .nickname(user.getNickname())
        .mileage(user.getMileage())
        .socials(user.getSocials())
        .build();

        return userProfileDTO;
    }

    // 프로필 수정
    public void updateUserProfile(UserProfileDTO userProfileDTO) {
        User user = userRepository.findById(userProfileDTO.getId()).get();

        user.setNickname(userProfileDTO.getNickname());
        user.setGenres(userProfileDTO.getGenres());
        userRepository.save(user);
    }

    // 아이디, 비밀번호 수정
    public String changeAccountInfo(SecurityUserDTO securityUserDTO) {
        // 아이디 비밀번호 교차 검증
        User user = userRepository.findByIdAndPassword(securityUserDTO.getId(), securityUserDTO.getPassword()); 

        // TODO : 이거 판별을 validation으로 가능한지 확인
        if (user == null) {
            return "입력하신 정보가 일치하지 않습니다.";
        }
        if (userRepository.existsById(securityUserDTO.getId())) {
            return "이미 존재하는 Id입니다.";            
        }
        user.changeAccountInfo(securityUserDTO.getId(), securityUserDTO.getPassword());
        return "변경되었습니다";
    }

    // 관심사 일치 여부 조회 counting 방식
}
