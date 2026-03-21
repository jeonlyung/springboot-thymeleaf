package com.project.springboot_thymeleaf.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
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

    /**
     * refreshToken:* prefix 에 해당하는 키를 모두 삭제
     * 운영 환경에서는 flushDb()보다 이 메소드 사용을 권장
     */
    public long deleteAllRefreshTokens() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys.isEmpty()) {
            return 0L;
        }

        long result = redisTemplate.delete(keys);
        log.warn("[Redis] Refresh Token 전체 삭제 - deletedCount: {}", result);
        return result;
    }

    /**
     * 현재 선택된 Redis DB의 모든 키를 삭제
     * 매우 위험하므로 관리자/개발 환경에서만 제한적으로 사용
     */
    public void flushCurrentDb() {
        Objects.requireNonNull(redisTemplate.getConnectionFactory(), "RedisConnectionFactory is null")
                .getConnection()
                .serverCommands()
                .flushDb();
        log.warn("[Redis] 현재 DB 전체 키 삭제(flushDb) 수행");
    }

    /**
     * 특정 Redis 키 1건 삭제
     * 예) refreshToken:user@email.com
     */
    public boolean deleteByKey(String key) {
        boolean result = redisTemplate.delete(key);
        log.info("[Redis] 특정 키 삭제 - key: {}, deleted: {}", key, result);
        return result;
    }
}
