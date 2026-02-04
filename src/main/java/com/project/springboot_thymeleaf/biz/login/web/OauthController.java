package com.project.springboot_thymeleaf.biz.login.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/oauth")
public class OauthController {

    @GetMapping("/kakao")
    public void kakaoLogin() {

    }
}
