package com.project.springboot_thymeleaf.biz;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        // templates 폴더의 index.html을 찾아서 보여줍니다.
        return "index";
    }

    @GetMapping("/main")
    public String main(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            if (oAuth2User instanceof CustomOAuth2User) {
                CustomOAuth2User customUser = (CustomOAuth2User) oAuth2User;
                model.addAttribute("username", customUser.getUsername());
                model.addAttribute("email", customUser.getEmail());
                log.info("로그인 사용자: {} ({})", customUser.getUsername(), customUser.getEmail());
            } else {
                model.addAttribute("username", oAuth2User.getAttribute("name"));
                model.addAttribute("email", oAuth2User.getAttribute("email"));
            }
        }
        return "main";
    }
}
