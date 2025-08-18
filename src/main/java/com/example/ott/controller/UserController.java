package com.example.ott.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.ott.customValidation.ValidationOrder;
import com.example.ott.dto.FollowedContentsDTO;
import com.example.ott.dto.SecurityUserDTO;
import com.example.ott.dto.TempSocialSignupDTO;
import com.example.ott.dto.TotalUserDTO;
import com.example.ott.dto.UserProfileDTO;
import com.example.ott.entity.Image;
import com.example.ott.service.FollowedContentsService;
import com.example.ott.service.ImageService;
import com.example.ott.service.TestService;
import com.example.ott.service.UserService;
import com.example.ott.type.SessionKeys;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller

@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ImageService imageService;
    private final FollowedContentsService followedContentsService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TestService testService;

    @GetMapping("/register")
    public String getRegister(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            TempSocialSignupDTO temp = (TempSocialSignupDTO) session.getAttribute(SessionKeys.TEMP_SOCIAL);
            if (temp != null && !model.containsAttribute("totalUserDTO")) {
                TotalUserDTO dto = new TotalUserDTO();
                dto.setName(temp.getName());
                dto.setNickname(temp.getNickname());
                dto.setEmail(temp.getEmail());
                dto.setGender(temp.getGender());
                model.addAttribute("totalUserDTO", dto);
            }
        }

        if (!model.containsAttribute("totalUserDTO")) {
            model.addAttribute("totalUserDTO", new TotalUserDTO());
        }

        return "user/register";
    }

    @PostMapping("/register")
    public String postRegister(
            @Validated(ValidationOrder.class) TotalUserDTO totalUserDTO,
            BindingResult result,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            HttpSession session, Model model) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.totalUserDTO", result);
            redirectAttributes.addFlashAttribute("totalUserDTO", totalUserDTO);
            // 플래시 속성은 redirect와 함께 써야 합니다.
            return "redirect:/user/register";
        }

        // 1) DB 저장 (및 필요한 후처리)
        userService.registerAndLogin(totalUserDTO);

        // 2) 소셜 플로우 여부 판단 (세션에 TEMP_SOCIAL 있으면 소셜)
        boolean isSocialFlow = (session != null && session.getAttribute(SessionKeys.TEMP_SOCIAL) != null);

        if (isSocialFlow) {
            // --- 소셜 회원가입: UserDetails 로드 -> 새 Authentication으로 교체 ---
            UserDetails userDetails = userDetailsService.loadUserByUsername(totalUserDTO.getId());

            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(newAuth);
            SecurityContextHolder.setContext(context);

            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // 임시 소셜 세션키 정리
            session.removeAttribute(SessionKeys.TEMP_SOCIAL);

        } else {
            // --- 일반 회원가입: 아이디/비번으로 인증 수행 ---
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            totalUserDTO.getId(),
                            totalUserDTO.getPassword()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            request.getSession(true).setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        }

        model.addAttribute("userId", totalUserDTO.getId());
        return "redirect:/";
    }

    // 프로필 조회
    @GetMapping("/userProfile")
    public String getUserProfile(String id, Model model, @RequestParam(defaultValue = "6") int size) {
        UserProfileDTO userProfileDTO = userService.getUserProfile(id);

        Pageable pageable = PageRequest.of(0, size);
        // 해당 user가 follow한 contents List 조회
        Page<FollowedContentsDTO> followedContentsList = followedContentsService.getFollowedContentsList(id, pageable);

        model.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("followedContentsList", followedContentsList);
        model.addAttribute("currentSize", size);
        model.addAttribute("hasMore", followedContentsList.getTotalElements() > size);

        return "user/userProfile";
    }

    @GetMapping("/modifyUserProfile")
    public String getModifyUserProfile(String id, Model model) {

        UserProfileDTO userProfileDTO = userService.getUserProfile(id);

        model.addAttribute("userProfileDTO", userProfileDTO);
        return "user/modifyUserProfile";
    }

    // 프로필 수정
    @PostMapping("modifyUserProfile")
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
    public String getLogin(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("securityUserDTO")) {
            model.addAttribute("securityUserDTO", new SecurityUserDTO());
        }

        Exception ex = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (ex != null) {
            model.addAttribute("loginErrorMessage", ex.getMessage());
            request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
        return "user/login";

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
    public String upgradeToAdmin(@AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            RedirectAttributes rttr) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "로그인이 필요합니다.");
        }

        // 1) DB에서 권한을 USER -> ADMIN(ROLE_ADMIN)으로 업데이트
        String username = userDetails.getUsername();
        userService.upgradeToAdmin(username);

        // 2) 최신 사용자 상태를 재로딩해서 새 Authentication으로 교체
        UserDetails refreshed = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                refreshed,
                null,
                refreshed.getAuthorities());
        newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(newAuth);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        // 3) 알림 후 프로필로 이동
        rttr.addFlashAttribute("msg", "관리자 권한이 부여되었습니다!");
        return "redirect:/user/userProfile?id=" + username;
    }

    @GetMapping("/userConsent")
    public void getUserConsent() {

    }

    @GetMapping(value = "/testCode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> makeUser() {
        // addTestUser()가 Long(생성 ID)을 리턴하면 아래처럼 담아주고,
        // void라면 id 관련 라인만 빼세요.
        testService.addTestUser();

        return ResponseEntity.ok(
                Map.of(
                        "status", "OK",
                        "action", "addTestUser"));
    }

}
