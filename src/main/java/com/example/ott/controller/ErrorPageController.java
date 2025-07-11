package com.example.ott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorPageController {

    @GetMapping("/emailAlreadyExists")
    public String getEmailAlreadyExists() {
        return "/error/emailAlreadyExists";
    }

}
