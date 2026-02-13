package com.project.springboot_thymeleaf.global.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 프로퍼티 (최신 방식: @ConfigurationProperties 사용)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "your-256-bit-secret-key-for-jwt-token-generation-change-this-in-production";
    private long accessTokenExpiration = 3600000; // 1시간
    private long refreshTokenExpiration = 604800000; // 7일
}

