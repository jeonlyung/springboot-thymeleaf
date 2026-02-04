package com.project.springboot_thymeleaf.global.security;

import com.project.springboot_thymeleaf.biz.login.web.OauthController;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> OauthController = null;
        
        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest()
                .permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // H2 콘솔 등을 쓸 경우 대비
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/", "/login/**", "/css/**", "/js/**", "/images/**").permitAll()
                                .anyRequest().authenticated() // 나머지는 인증 필요
                        )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // 커스텀 로그인 페이지 사용 시
                        .defaultSuccessUrl("/main", true) // 로그인 성공 후 이동할 페이지
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(OauthController) // 카카오 정보를 받아올 서비스 등록 ㅡㅡ+
                        )
                ).logout(logout -> logout
                        .logoutSuccessUrl("/")
                );

        return httpSecurity.build();
    }
}
