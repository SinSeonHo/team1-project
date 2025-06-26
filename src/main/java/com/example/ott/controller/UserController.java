package com.example.ott.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.Image;
import com.example.ott.service.FavoriteService;
import com.example.ott.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final FavoriteService favoriteService;

    // // 회원가입 페이지 호출
    // @GetMapping("/register")
    // public void getRegister() {

    // // 추후 회원가입 페이지 반영
    // }
    // 회원가입 요청

    // @ResponseBody
    @PostMapping("/register")
    public String postRegister(
            @ModelAttribute @Valid SecurityUserDTO securityUserDTO,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes rttr) {
        if (bindingResult.hasErrors()) {
            return "/user/login";
        } else {

            String id = userService.registerAndLogin(securityUserDTO, request);
            try {
                request.login(securityUserDTO.getId(), securityUserDTO.getPassword());
            } catch (ServletException e) {
                log.info("로그인 실패");
                e.printStackTrace();
            }

            rttr.addAttribute("id", id);
        }
        return "redirect:/user/modifyUserProfile";
    }

    // 프로필 조회
    @GetMapping("/userProfile")
    public String getUserProfile(String id, Model model) {

        UserProfileDTO userProfileDTO = userService.getUserProfile(id);
        log.info("user Profile 조회 : {}", userProfileDTO);
        model.addAttribute("userProfileDTO", userProfileDTO);

        // 유저가 팔로우 한 콘텐츠들 사진 정보
        List<Image> images = favoriteService.getFollowedContentsImages(userProfileDTO.getId());
        model.addAttribute("images", images);

        return "/user/userProfile";
    }

    @GetMapping("/modifyUserProfile")
    public String getModifyUserProfile(String id, Model model) {

        UserProfileDTO userProfileDTO = userService.getUserProfile(id);
        log.info("user Profile 조회 : {}", userProfileDTO);
        model.addAttribute("userProfileDTO", userProfileDTO);
        return "/user/modifyUserProfile";
    }

    // 프로필 수정
    @PostMapping("/modifyUserProfile")
    public String postUserProfile(@Valid UserProfileDTO userProfileDTO, BindingResult bindingResult,
            RedirectAttributes rttr) {

        if (bindingResult.hasErrors()) {
            return "/user/modifyUserProfile";
        } else {
            userService.updateUserProfile(userProfileDTO);

            log.info("변경된 userProfile 정보 {}", userProfileDTO);
            rttr.addAttribute("id", userProfileDTO.getId());

            return "redirect:/user/userProfile";
        }
    }

    @GetMapping("delete")
    public String getDelete(String id) {
        userService.deleteUser(id);

        return "redirect:/logout";
    }

    @GetMapping("/login")
    public String getLogin(Model model) {
        if (!model.containsAttribute("securityUserDTO")) {
            model.addAttribute("securityUserDTO", new SecurityUserDTO());
        }
        return "/user/login";
    }

}