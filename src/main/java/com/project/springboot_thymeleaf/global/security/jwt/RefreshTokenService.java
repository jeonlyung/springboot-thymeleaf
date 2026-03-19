package com.project.springboot_thymeleaf.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Refresh Token Redis 저장소
 *
 * Redis Key 구조: "refreshToken:{email}" → refreshToken 문자열
 * TTL: 7일 (JwtProperties.refreshTokenExpiration 과 동기화)
 *
 * 사용 예시:
 *   refreshTokenService.save("user@email.com", refreshToken);   // 저장
 *   refreshTokenService.get("user@email.com");                   // 조회
 *   refreshTokenService.validate("user@email.com", token);       // 검증
 *   refreshTokenService.delete("user@email.com");                // 삭제(로그아웃)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refreshToken:";
    private static final long TTL_DAYS = 7L;

    private final RedisTemplate<String, Object> redisTemplate;

    /** Refresh Token 저장 (로그인 시) */
    public void save(String email, String refreshToken) {
        String key = KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, refreshToken, TTL_DAYS, TimeUnit.DAYS);
        log.info("[Redis] Refresh Token 저장 - key: {}", key);
    }

    /** Refresh Token 조회 */
    public String get(String email) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        return value != null ? value.toString() : null;
    }

    /** Refresh Token 유효성 검증 (DB 없이 Redis만으로) */
    public boolean validate(String email, String refreshToken) {
        String saved = get(email);
        return refreshToken.equals(saved);
    }

    /** Refresh Token 삭제 (로그아웃 / 재발급 시) */
    public void delete(String email) {
        String key = KEY_PREFIX + email;
        redisTemplate.delete(key);
        log.info("[Redis] Refresh Token 삭제 - key: {}", key);
    }
}

