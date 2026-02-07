package com.project.springboot_thymeleaf.global.security;

import com.project.springboot_thymeleaf.biz.login.service.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OauthService oauthService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // 권한 설정(좁은 범위부터 넓은 범위로 설정해야함)
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 및 로그인 관련 경로는 누구나 접근 가능
                        .requestMatchers("/", "/login/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 일반 로그인(ID/PW) 설정
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login/process")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/main", true) // 성공하면 바로 메인으로
                        .permitAll()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/main", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService((oauthService))
                        )
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                );

        return httpSecurity.build();
    }
}
