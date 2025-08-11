package com.example.ott.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ott.customValidation.ValidationOrder;
import com.example.ott.dto.FollowedContentsDTO;
import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.dto.TotalUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.FollowedContents;
import com.example.ott.entity.Image;
import com.example.ott.entity.UserRole;
import com.example.ott.handler.AuthSuccessHandler;
import com.example.ott.security.CustomUserDetails;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.ImageService;
import com.example.ott.service.UserService;
import com.example.ott.type.SessionKeys;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ImageService imageService;
    private final FollowedContentsService followedContentsService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/register")
    public String getRegister(Model model, HttpServletRequest request) {

        // 1) 세션에서 TEMP_SOCIAL 꺼내기 (있을 때만)
        HttpSession session = request.getSession(false); // 세션 없으면 null
        if (session != null) {
            TempSocialSignupDTO temp = (TempSocialSignupDTO) session.getAttribute(SessionKeys.TEMP_SOCIAL);
            if (temp != null && !model.containsAttribute("totalUserDTO")) {
                // 세션의 임시 소셜 데이터를 TotalUserDTO에 매핑
                TotalUserDTO dto = new TotalUserDTO();

                dto.setName(temp.getName());
                dto.setNickname(temp.getNickname());
                dto.setEmail(temp.getEmail());
                dto.setGender(temp.getGender());

                model.addAttribute("totalUserDTO", dto);
            }
        }

        // 2) 세션 없거나 tempDTO 없으면 기존 방식 유지
        if (!model.containsAttribute("totalUserDTO")) {
            TotalUserDTO dto = new TotalUserDTO();
            model.addAttribute("totalUserDTO", dto);
        }

        return "user/register";
    }

    @PostMapping("/register")
    public String postRegister(@Validated(ValidationOrder.class) TotalUserDTO totalUserDTO, BindingResult result,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            HttpSession session, Model model) {

        if (result.hasErrors()) {
            // 에러 메시지나 입력값을 FlashAttribute로 임시 저장
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.totalUserDTO", result);
            redirectAttributes.addFlashAttribute("totalUserDTO", totalUserDTO);
            return "/user/register";
        }

        // DB에 회원정보 저장
        userService.registerAndLogin(totalUserDTO);

        // 2) 소셜(PENDING) 가입인지 여부 판단
        boolean isSocialFlow = (session != null && session.getAttribute(SessionKeys.TEMP_SOCIAL) != null);
        if (isSocialFlow) {
            // ---- 소셜 회원가입: 현재 세션은 이미 인증됨(PENDING). 권한만 USER로 갱신 ----
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            if (current != null && current.getPrincipal() instanceof CustomUserDetails cud) {
                cud.getSecurityUserDTO().setUserRole(UserRole.USER); // 권한 업데이트
                UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(cud,
                        current.getCredentials(), cud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                request.getSession().setAttribute(
                        org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());

                session.removeAttribute(SessionKeys.TEMP_SOCIAL);
            }

        } else {

            // 일반 회원가입 AuthenticationManager로 인증
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(totalUserDTO.getId(), totalUserDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
        }
        model.addAttribute("userId", totalUserDTO.getId());

        return "redirect:/";
    }

    // 프로필 조회
    @GetMapping("/userProfile")
    public String getUserProfile(String id, Model model, @RequestParam(defaultValue = "6") int size) {
        UserProfileDTO userProfileDTO = userService.getUserProfile(id);
        log.info("유저 닉네임 : {}", userProfileDTO.getNickname());
        Pageable pageable = PageRequest.of(0, size);
        // 해당 user가 follow한 contents List 조회
        Page<FollowedContentsDTO> followedContentsList = followedContentsService.getFollowedContentsList(id, pageable);

        log.info("이미지 확인");

        model.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("followedContentsList", followedContentsList);
        model.addAttribute("currentSize", size);
        model.addAttribute("hasMore", followedContentsList.getTotalElements() > size);

        return "/user/userProfile";
    }

    @GetMapping("/modifyUserProfile")
    public String getModifyUserProfile(String id, Model model) {

        UserProfileDTO userProfileDTO = userService.getUserProfile(id);

        model.addAttribute("userProfileDTO", userProfileDTO);
        return "/user/modifyUserProfile";
    }

    // 프로필 수정
    @PostMapping("/modifyUserProfile")
    public String postUserProfile(@Valid UserProfileDTO userProfileDTO, BindingResult bindingResult,
            RedirectAttributes rttr, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("userProfileDTO", userProfileDTO);
            return "/user/modifyUserProfile";
        } else {
            userService.updateUserProfile(userProfileDTO);

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
        try {
            Image savedThumbnail = imageService.uploadThumbnailImage(file);
            userService.saveUserProfile(savedThumbnail, id);

        } catch (IOException e) {
            e.printStackTrace(); // 이걸 추가!
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

    @GetMapping("/userConsent")
    public void getUserConsent() {

    }

    @GetMapping("/loginTest")
    public void getMethodName() {

    }

}