package com.project.springboot_thymeleaf.biz;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import com.project.springboot_thymeleaf.biz.login.dto.MemberDto;
import com.project.springboot_thymeleaf.biz.login.mapper.LoginMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final LoginMapper loginMapper;

    @GetMapping("/")
    public String index() {
        return "main/index";
    }

    @GetMapping("/main")
    public String main(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        if (oAuth2User != null) {
            String username = null;
            String email = null;
            String provider = null;
            String providerId = null;

            if (oAuth2User instanceof CustomOAuth2User customUser) {
                username = customUser.getUsername();
                email = customUser.getEmail();
                provider = customUser.getProvider();
                providerId = customUser.getProviderId();
            } else {
                username = oAuth2User.getAttribute("name");
                email = oAuth2User.getAttribute("email");
            }

            // DB에서 사용자 정보 조회
            if (provider != null && providerId != null) {
                Map<String, String> params = new HashMap<>();
                params.put("provider", provider);
                params.put("providerId", providerId);

                MemberDto member = loginMapper.findByProviderAndProviderId(params);

                if (member != null) {
                    model.addAttribute("member", member);
                    model.addAttribute("usrNm", member.usrNm());           // 테이블 컬럼명으로 매핑
                    model.addAttribute("email", email);
                    model.addAttribute("profile_img", member.profileImg()); // 테이블 컬럼명으로 매핑
                    model.addAttribute("provider", member.provider());
                    model.addAttribute("userId", member.usrId());

                    log.info("DB 사용자 정보 조회 성공: {} ({})", member.usrNm(), member.usrId());
                } else {
                    model.addAttribute("usrNm", username);
                    model.addAttribute("email", email);
                    log.warn("DB에서 사용자 정보를 찾을 수 없음: {} / {}", provider, providerId);
                }
            } else {
                model.addAttribute("usrNm", username);
                model.addAttribute("email", email);
            }
        }

        return "main/main";
    }
}
