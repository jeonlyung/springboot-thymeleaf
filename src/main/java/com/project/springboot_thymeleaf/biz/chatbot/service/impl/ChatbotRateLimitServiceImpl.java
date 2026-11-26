package com.project.springboot_thymeleaf.biz.chatbot.service;

import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 키(IP) 별 분당 고정 윈도우 제한. 단일 인스턴스용 — 다중 인스턴스면 Redis 로 옮길 것. */
@Component
@RequiredArgsConstructor
public class ChatRateLimiter {

    private record Window(long minute, AtomicInteger count) {}

    private final ChatbotProperties props;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        return tryAcquire(key, System.currentTimeMillis());
    }

    public boolean tryAcquire(String key, long nowMillis) {
        long minute = nowMillis / 60_000;
        if (windows.size() > 10_000) {
            windows.values().removeIf(w -> w.minute() < minute);
        }
        Window w = windows.compute(key, (k, old) ->
                old == null || old.minute() != minute ? new Window(minute, new AtomicInteger()) : old);
        return w.count().incrementAndGet() <= props.getRateLimitPerMinute();
    }
}
