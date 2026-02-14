package com.project.springboot_thymeleaf.global.security.filter;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import com.project.springboot_thymeleaf.biz.login.dto.MemberDto;
import com.project.springboot_thymeleaf.global.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JWT 인증 필터 (2026 최신 방식)
 * - Cookie에서 JWT 추출
 * - 토큰 검증 후 SecurityContext에 인증 정보 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromCookie(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
                Claims claims = jwtTokenProvider.getClaims(jwt);
                String email = claims.get("email", String.class);
                String provider = claims.get("provider", String.class);
                String authoritiesStr = claims.get("authorities", String.class);
                String name = claims.get("name", String.class);
                String profileImg = claims.get("profileImg", String.class);
                String providerId = claims.get("providerId", String.class);

                // 권한 정보 파싱
                List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesStr.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // MemberDto 생성
                MemberDto memberDto = MemberDto.builder()
                        .usrId(email)
                        .usrNm(name)
                        .profileImg(profileImg)
                        .provider(provider)
                        .providerId(providerId)
                        .useYn("Y")
                        .build();

                // OAuth2 attributes 생성
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("email", email);
                attributes.put("name", name);
                attributes.put("profileImg", profileImg);
                attributes.put("provider", provider);
                attributes.put("providerId", providerId);

                // CustomOAuth2User 생성 (3개의 파라미터)
                CustomOAuth2User oAuth2User = new CustomOAuth2User(
                        memberDto,
                        attributes,
                        "email"
                );

                // Spring Security 인증 객체 생성 (principal을 CustomOAuth2User로 설정)
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(oAuth2User, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT 인증 성공: {} (Provider: {})", email, provider);
            }
        } catch (Exception e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Cookie에서 JWT 추출
     */
    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

