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
        log.info("=== /main 진입 ===");
        log.info("OAuth2User null 여부: {}", (oAuth2User == null));

        if (oAuth2User != null) {
            log.info("OAuth2User 타입: {}", oAuth2User.getClass().getName());
            log.info("OAuth2User attributes: {}", oAuth2User.getAttributes());

            String username = null;
            String email = null;
            String provider = null;
            String providerId = null;

            if (oAuth2User instanceof CustomOAuth2User customUser) {
                log.info("CustomOAuth2User로 캐스팅 성공");
                username = customUser.getUsername();
                email = customUser.getEmail();
                provider = customUser.getProvider();
                providerId = customUser.getProviderId();

                log.info("username: {}, email: {}, provider: {}, providerId: {}",
                    username, email, provider, providerId);
            } else {
                log.info("기본 OAuth2User 사용");
                username = oAuth2User.getAttribute("name");
                email = oAuth2User.getAttribute("email");
            }

            // DB에서 사용자 정보 조회
            if (provider != null && providerId != null) {
                Map<String, String> params = new HashMap<>();
                params.put("provider", provider);
                params.put("providerId", providerId);

                log.info("DB 조회 파라미터: provider={}, providerId={}", provider, providerId);
                MemberDto member = loginMapper.findByProviderAndProviderId(params);

                if (member != null) {
                    log.info("DB 사용자 정보 조회 성공: {}", member);
                    log.info("이름: {}, 프로필 이미지: {}", member.usrNm(), member.profileImg());

                    model.addAttribute("member", member);
                    model.addAttribute("usrNm", member.usrNm());           // 테이블 컬럼명으로 매핑
                    model.addAttribute("email", email);
                    model.addAttribute("profile_img", member.profileImg()); // 테이블 컬럼명으로 매핑
                    model.addAttribute("provider", member.provider());
                    model.addAttribute("userId", member.usrId());

                    log.info("Model에 데이터 추가 완료");
                } else {
                    model.addAttribute("usrNm", username);
                    model.addAttribute("email", email);
                    log.warn("DB에서 사용자 정보를 찾을 수 없음: {} / {}", provider, providerId);
                }
            } else {
                model.addAttribute("usrNm", username);
                model.addAttribute("email", email);
            }
        } else {
            log.warn("OAuth2User가 null입니다. 인증 정보가 세션에 없습니다.");
            log.warn("로그인 페이지로 리다이렉트합니다.");
            return "redirect:/login";
        }

        return "main/main";
    }
}
