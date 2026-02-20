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
import java.util.Optional;

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
        log.info("=== 메인 페이지 진입 ===");

        // OAuth2 인증 확인
        if (oAuth2User == null) {
            log.warn("⚠️ 인증되지 않은 사용자 접근 시도");
            return "redirect:/login";
        }

        // 사용자 정보 추출 및 모델에 추가
        extractUserInfo(oAuth2User)
            .ifPresentOrElse(
                userInfo -> {
                    log.info("✅ 사용자 정보 추출 성공: {}", userInfo.get("email"));
                    addUserInfoToModel(userInfo, model);
                },
                () -> {
                    log.warn("⚠️ 사용자 정보 추출 실패");
                    model.addAttribute("error", "사용자 정보를 불러올 수 없습니다.");
                }
            );

        return "main/main";
    }

    /**
     * OAuth2User에서 사용자 정보 추출
     */
    private Optional<Map<String, Object>> extractUserInfo(OAuth2User oAuth2User) {
        try {
            Map<String, Object> userInfo = new HashMap<>();

            if (oAuth2User instanceof CustomOAuth2User customUser) {
                log.debug("📱 CustomOAuth2User 타입 감지");
                userInfo.put("username", customUser.getUsername());
                userInfo.put("email", customUser.getEmail());
                userInfo.put("provider", customUser.getProvider());
                userInfo.put("providerId", customUser.getProviderId());
                userInfo.put("profileImg", customUser.getProfileImg());
            } else {
                log.debug("🌐 기본 OAuth2User 타입 사용");
                userInfo.put("username", oAuth2User.getAttribute("name"));
                userInfo.put("email", oAuth2User.getAttribute("email"));
            }

            return Optional.of(userInfo);
        } catch (Exception e) {
            log.error("❌ 사용자 정보 추출 중 오류 발생", e);
            return Optional.empty();
        }
    }

    /**
     * 모델에 사용자 정보 추가
     */
    private void addUserInfoToModel(Map<String, Object> userInfo, Model model) {
        String provider = (String) userInfo.get("provider");
        String providerId = (String) userInfo.get("providerId");

        // DB에서 회원 정보 조회
        if (provider != null && providerId != null) {
            findMemberFromDB(provider, providerId)
                .ifPresentOrElse(
                    member -> {
                        log.info("💾 DB 회원 정보 조회 성공: {} ({})", member.usrNm(), provider);
                        addMemberToModel(member, model, (String) userInfo.get("email"));
                    },
                    () -> {
                        log.warn("⚠️ DB에 회원 정보 없음: {} / {}", provider, providerId);
                        addBasicInfoToModel(userInfo, model);
                    }
                );
        } else {
            log.debug("ℹ️ OAuth 제공자 정보 없음 - 기본 정보만 사용");
            addBasicInfoToModel(userInfo, model);
        }
    }

    /**
     * DB에서 회원 정보 조회
     */
    private Optional<MemberDto> findMemberFromDB(String provider, String providerId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("provider", provider);
            params.put("providerId", providerId);

            MemberDto member = loginMapper.findByProviderAndProviderId(params);
            return Optional.ofNullable(member);
        } catch (Exception e) {
            log.error("❌ DB 조회 중 오류 발생: provider={}, providerId={}", provider, providerId, e);
            return Optional.empty();
        }
    }

    /**
     * 회원 정보를 모델에 추가
     */
    private void addMemberToModel(MemberDto member, Model model, String email) {
        model.addAttribute("member", member);
        model.addAttribute("userId", member.usrId());
        model.addAttribute("usrNm", member.usrNm());
        model.addAttribute("email", email != null ? email : member.usrId());
        model.addAttribute("provider", member.provider());

        log.debug("📊 모델 데이터: 이름={}, 프로필={}, 제공자={}",
                 member.usrNm(),
                 member.profileImg() != null ? "O" : "X",
                 member.provider());
    }

    /**
     * 기본 사용자 정보를 모델에 추가
     */
    private void addBasicInfoToModel(Map<String, Object> userInfo, Model model) {
        model.addAttribute("usrNm", userInfo.get("username"));
        model.addAttribute("email", userInfo.get("email"));
        model.addAttribute("provider", userInfo.get("provider"));
    }
}
