package com.example.ott.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.matrix.service.AudSessionService;
import com.matrix.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class AudController {

    // defaultValue 는 AUD 사용자 : matrix(교사용꺼)
    // 팀계정으로 바꾸면 사용가능

    @GetMapping("/report")
    public String iaudEmbedded(String rCode, String sTitle,
            @RequestParam(defaultValue = "user02", required = false) String userCode, HttpServletRequest request,
            HttpServletResponse response,
            Model model) throws Exception {

        log.info("rCode {}", rCode);

        /*********************************************
         * 사용자 설정 영역
         * serverUrl: AUD7 플랫폼 matrix 서버 주소
         * user_id : 임베디드 시 연동할 유저코드
         * is_ssl
         **********************************************/
        String serverUrl = "http://192.168.1.1:8087";

        /*********************************************
         * 사용자 코드에 대한 커스텀 방법
         * userCode : AUD7 에서 승인된 userCode
         * 내부적으로는 AUD7 서버에 SSO_AUTH_IP 를 등록해야 한다.
         * 
         * 전달한 userCode를 복호화 처리 하여 AUD7플랫폼에 등록된 평문 userCode를 설정한다.
         * 이 부분은 request.setAttribute() 반드시 해야 함 execute() 호출 시 이 값 이용
         **********************************************/

        request.setAttribute("aud7_server_url", serverUrl);
        request.setAttribute("is_ssl", false);
        request.setAttribute("user_id", userCode);

        // 1. AUD7 플랫폼용 token을 발생한다.
        String aud7Token = null;
        try {
            aud7Token = new TokenService().execute(request, response);
            System.out.println("aud7Token: " + aud7Token);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return null;
        }

        // 2. 인증정보에 저장된 path 일부 항목을 조회하여 script를 import 한다.
        model.addAttribute("token", aud7Token);
        Map<String, String> audSessionInfo = null;
        try {
            audSessionInfo = new AudSessionService().execute(request, response);
            System.out.println("audSessionInfo: " + audSessionInfo);
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return null;
        }
        String AUD7_FULL_PATH = audSessionInfo.get("AUD7_FULL_PATH");
        String AUD7_SKIN_CSS_PATH = audSessionInfo.get("AUD7_SKIN_CSS_PATH");
        String CUSTOM_PARAM = audSessionInfo.get("CUSTOM_PARAM");
        String PORTAL_THEME_CSS_PATH = audSessionInfo.get("PORTAL_THEME_CSS_PATH");

        model.addAttribute("serverUrl", serverUrl);
        model.addAttribute("userCode", userCode);
        model.addAttribute("rCode", rCode);
        model.addAttribute("showTitle", sTitle);
        model.addAttribute("aud7Token", aud7Token);

        model.addAttribute("AUD7_FULL_PATH", AUD7_FULL_PATH);
        model.addAttribute("AUD7_SKIN_CSS_PATH", AUD7_SKIN_CSS_PATH);
        model.addAttribute("CUSTOM_PARAM", CUSTOM_PARAM);
        model.addAttribute("PORTAL_THEME_CSS_PATH", PORTAL_THEME_CSS_PATH);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // thymeleaf 과 같이 사용하기 때문에 강제 지정
        return "forward:/WEB-INF/views/report.jsp";
    }

}
