package com.example.ott.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class ChartController {
    @GetMapping("/chart")
    public String showChart() {
        return "chart/chart"; // templates/chart/chart.html 렌더링
    }
}
