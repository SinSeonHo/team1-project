package com.example.ott.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.Image;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.ImageService;
import com.example.ott.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller

@Log4j2
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FollowedContentsService favoriteService;
    private final ImageService imageService;
    private final FollowedContentsService followedContentsService;

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
                request.login(securityUserDTO.getId(), securityUserDTO.getPassword()); // 로그인 요청
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
        log.info("유저 권한 : {}", userProfileDTO.getGrade());

        // 해당 user가 follow한 contents List 조회
        List<FollowedContents> followedContentsList = followedContentsService
                .getFollowedContentsList(userProfileDTO.getNickname());

        model.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("followedContentsList", followedContentsList);

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
            RedirectAttributes rttr, Model model) {
        log.info("userProfile 정보 : {}", userProfileDTO);

        if (bindingResult.hasErrors()) {
            model.addAttribute("userProfileDTO", userProfileDTO);
            return "/user/modifyUserProfile";
        } else {
            userService.updateUserProfile(userProfileDTO);

            log.info("변경된 userProfile 정보 {}", userProfileDTO);
            rttr.addAttribute("id", userProfileDTO.getId());

            return "redirect:/user/userProfile";
        }
    }

    @PostMapping("/delete")
    public String deleteUser(String id, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        userService.deleteUser(id);

        request.logout(); // 로그아웃 요청
        return "redirect:/";
    }

    @GetMapping("/login")
    public String getLogin(Model model) {
        if (!model.containsAttribute("securityUserDTO")) {
            model.addAttribute("securityUserDTO", new SecurityUserDTO());
        }
        return "/user/login";
    }

    // 프로필 사진 업로드
    @PostMapping("/uploadProfile")
    public String postUploadProfile(@RequestParam("file") MultipartFile file, String id, RedirectAttributes rttr) {
        System.out.println("찍히나?");
        log.info("image upload 시도 {}", file);
        try {
            Image savedThumbnail = imageService.uploadThumbnailImage(file);
            log.info("user에 이미지 정보 주입 시도 {}, userId: {}", savedThumbnail, id);
            userService.saveUserProfile(savedThumbnail, id);

        } catch (IOException e) {
            e.printStackTrace(); // 이걸 추가!
            System.out.println("일단 에러났어요.");
        }

        rttr.addAttribute("id", id);
        return "redirect:/user/userProfile?img=updated";
    }

    // 어드민 권한 주소
    @GetMapping("/upgrade")
    public String upgradeToAdmin(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes rttr) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "로그인이 필요합니다.");
        }

        userService.upgradeToAdmin(userDetails.getUsername());
        rttr.addFlashAttribute("msg", "관리자 권한이 부여되었습니다!");

        return "redirect:/user/userProfile?id=" + userDetails.getUsername();
    }

}