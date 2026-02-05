package com.project.springboot_thymeleaf.biz.login.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping("")
    public String goLogin() {
        return "login/login";
    }

    @PostMapping("/process")
    public String processLogin() {

        return "login/login";
    }
}
