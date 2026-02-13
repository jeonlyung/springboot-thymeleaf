package com.project.springboot_thymeleaf.global.security;

import com.project.springboot_thymeleaf.biz.login.service.OauthService;
import com.project.springboot_thymeleaf.global.security.filter.JwtAuthenticationFilter;
import com.project.springboot_thymeleaf.global.security.handler.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 (2026 최신 방식)
 * - JWT 기반 Stateless 인증
 * - SessionCreationPolicy.STATELESS
 * - OAuth2 + JWT 통합
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OauthService oauthService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (Stateless - 최신 트렌드)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Frame Options 설정
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // 권한 설정 (좁은 범위 → 넓은 범위)
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 및 로그인 관련 경로는 누구나 접근 가능
                        .requestMatchers("/", "/login/**", "/oauth2/**",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // 일반 로그인(ID/PW) 설정 - 비활성화 (OAuth2만 사용)
                .formLogin(form -> form.disable())

                // OAuth2 로그인 설정 (최신 방식)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler) // JWT 생성 핸들러
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauthService) // 커스텀 OAuth2 서비스
                        )
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("access_token", "refresh_token") // JWT 쿠키 삭제
                        .invalidateHttpSession(true)
                )

                // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전에 실행)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
