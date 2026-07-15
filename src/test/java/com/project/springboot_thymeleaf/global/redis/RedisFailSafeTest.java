package com.project.springboot_thymeleaf.global.redis;

import com.project.springboot_thymeleaf.global.config.RedisHealthController;
import com.project.springboot_thymeleaf.global.security.jwt.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.ui.ExtendedModelMap;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisFailSafeTest {

    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> mockRedisTemplate() {
        return (RedisTemplate<String, Object>) mock(RedisTemplate.class);
    }

    @Test
    void dashboardRendersWithDefaultsWhenRedisIsUnavailable() {
        RedisTemplate<String, Object> redisTemplate = mockRedisTemplate();
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        ExtendedModelMap model = new ExtendedModelMap();

        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Unable to connect to Redis"));
        when(redisTemplate.keys("*")).thenThrow(new RuntimeException("Unable to connect to Redis"));

        RedisHealthController controller = new RedisHealthController(redisTemplate);

        String viewName = assertDoesNotThrow(() -> controller.dashboard(model));

        assertEquals("redis/dashboard", viewName);
        assertEquals(Boolean.FALSE, model.getAttribute("connected"));
        assertEquals("FAIL", model.getAttribute("pingResult"));
        assertEquals(0L, model.getAttribute("dbSize"));
        assertEquals("Unable to connect to Redis", model.getAttribute("redisError"));
        assertEquals(Collections.emptyList(), model.getAttribute("keyList"));
    }

    @Test
    void refreshTokenSaveAndGetDoNotThrowWhenRedisIsUnavailable() {
        RedisTemplate<String, Object> redisTemplate = mockRedisTemplate();
        @SuppressWarnings("unchecked")
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Unable to connect to Redis"))
                .when(valueOperations)
                .set(eq("refreshToken:user@test.com"), eq("sample-token"), eq(7L), eq(TimeUnit.DAYS));
        when(valueOperations.get("refreshToken:user@test.com"))
                .thenThrow(new RuntimeException("Unable to connect to Redis"));

        RefreshTokenService service = new RefreshTokenService(redisTemplate);

        assertDoesNotThrow(() -> service.save("user@test.com", "sample-token"));
        assertNull(service.get("user@test.com"));
    }
}



