package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {

    // 메인 페이지
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    // 로그인 처리 (일단 간단하게)
    @PostMapping("/login")
    public String login() {
        return "redirect:/dashboard";
    }

    // 대시보드 (조회 메인)
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}