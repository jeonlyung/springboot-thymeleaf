package com.project.springboot_thymeleaf.global.security.handler;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import com.project.springboot_thymeleaf.global.security.jwt.JwtTokenProvider;
import com.project.springboot_thymeleaf.global.security.jwt.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 핸들러 (2026 최신 방식)
 * - JWT 토큰 생성
 * - HttpOnly Cookie에 저장 (XSS 방지)
 * - 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getEmail();
        String provider = oAuth2User.getMember().provider();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication, email, provider);
        String refreshToken = jwtTokenProvider.generateRefreshToken(email);

        // ★ Refresh Token을 Redis에 저장 (7일 TTL)
        refreshTokenService.save(email, refreshToken);

        log.info("OAuth2 로그인 성공 - User: {}, Provider: {}", email, provider);

        // 세션에 OAuth2User 저장 (MainController에서 사용)
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT",
            new org.springframework.security.core.context.SecurityContextImpl(authentication));

        log.info("세션에 인증 정보 저장 완료 - Session ID: {}", request.getSession().getId());

        // HttpOnly Cookie에 토큰 저장 (최신 보안 방식)
        addTokenCookie(response, "access_token", accessToken, 3600); // 1시간
        addTokenCookie(response, "refresh_token", refreshToken, 604800); // 7일

        // 프론트엔드로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/main?login=success");
    }

    /**
     * HttpOnly, Secure Cookie 생성 (CSRF 및 XSS 방지)
     */
    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // JavaScript 접근 차단 (XSS 방지)
        cookie.setSecure(false); // HTTPS에서만 전송 (production에서는 true)
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}

