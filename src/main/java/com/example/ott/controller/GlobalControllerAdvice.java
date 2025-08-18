package com.example.ott.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.ott.entity.User;
import com.example.ott.exception.ReportActionException;
import com.example.ott.service.UserService;

import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserService userService;

    // header에 이미지 정보 상시 추가
    @ModelAttribute
    public void currentUser(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User user = userService.getUserById(userDetails.getUsername());
            if (user == null)
                return;
            String id = user.getId();
            if (user.getImage() != null) {
                model.addAttribute("profileImageUrl", user.getImage().getThumbnailPath());

            }
            model.addAttribute("userId", id);
        }
    }

    // 커스텀 에러
    @ExceptionHandler(NoSuchElementException.class)
    public String noSuchUserException() {

        return "error/noSuchUser";
    }

    /** 신고/조치 도메인 예외: 프론트에서 alert로 바로 띄우기 좋게 text/plain 으로 메시지 반환 */
    @ExceptionHandler(ReportActionException.class)
    public ResponseEntity<String> handleReportActionException(ReportActionException ex) {
        return ResponseEntity
                .status(HttpStatus.LOCKED) // 423 Locked (또는 CONFLICT 409)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage()); // 프런트에서 res.text()로 그대로 alert
    }
}
