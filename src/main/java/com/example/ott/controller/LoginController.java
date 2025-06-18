package com.example.ott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.log4j.Log4j2;


@Controller
@Log4j2
public class LoginController {
    
    @GetMapping("/login")
    public String getLogin() {
        log.info("login 페이지 요청");
        return "/socialTest/login";
    }
    
}
