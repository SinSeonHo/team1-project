package com.example.ott.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller

@Log4j2
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // // 회원가입 페이지 호출
    // @GetMapping("/register")
    // public void getRegister() {

    // // 추후 회원가입 페이지 반영
    // }
    // 회원가입 요청

    // @ResponseBody
    @PostMapping("/register")
    public String postRegister(
            @ModelAttribute SecurityUserDTO securityUserDTO,
            HttpServletRequest request) {
        userService.registerAndLogin(securityUserDTO, request);
        try {
            request.login(securityUserDTO.getId(), securityUserDTO.getPassword());
        } catch (ServletException e) {
            log.info("로그인 실패");
            e.printStackTrace();
        }
        return "redirect:/";
    }

    // login page 호출

    // login 요청 = 시큐리티가 해준다!

    // 프로필 조회
    @GetMapping({ "/userProfile", "/modifyUserProfile" })
    public void getUserProfile(String id, Model model) {
        UserProfileDTO userProfileDTO = userService.getUserProfile(id);
        log.info("user Profile 조회 : {}", userProfileDTO);
        // rttr.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("userProfileDTO", userProfileDTO);

    }

    // 프로필 수정
    @PostMapping("/modifyUserProfile")
    public String postUserProfile(UserProfileDTO userProfileDTO, RedirectAttributes rttr) {
        userService.updateUserProfile(userProfileDTO);

        log.info("변경된 userProfile 정보 {}", userProfileDTO);
        rttr.addAttribute("id", userProfileDTO.getId());

        return "redirect:/user/userProfile";
    }

    @GetMapping("/login")
    public void getLogin() {
    }

}
