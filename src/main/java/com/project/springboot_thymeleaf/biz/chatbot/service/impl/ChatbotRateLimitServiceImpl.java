package com.project.springboot_thymeleaf.biz.chatbot.service.impl;

import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import com.project.springboot_thymeleaf.biz.chatbot.service.ChatbotRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 챗봇 호출 횟수 제한 서비스 구현체
 * 키(IP) 별로 "지금 몇 분인지"와 그 분에 몇 번 불렀는지를 메모리에 들고 있다가, 한도(chatbot.rate-limit-per-minute)를 넘으면 거절한다.
 * 서버 메모리에만 저장하기 때문에 서버가 여러 대면 대수만큼 한도가 늘어난다. 그때는 Redis 로 옮길 것.
 */
@Service
@RequiredArgsConstructor
public class ChatbotRateLimitServiceImpl implements ChatbotRateLimitService {

    /** 한 분 단위 카운터. minute 이 바뀌면 새로 시작한다. */
    private record Window(long minute, AtomicInteger count) {}

    /** 맵이 이 크기를 넘으면 지난 분 항목을 정리한다. (IP 가 계속 늘어서 메모리가 커지는 것 방지) */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final ChatbotProperties props;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key) {
        return tryAcquire(key, System.currentTimeMillis());
    }

    /** 현재 시각을 직접 넘길 수 있는 버전. 테스트에서 시간을 흘려보내려고 따로 열어 뒀다. */
    public boolean tryAcquire(String key, long nowMillis) {
        long minute = nowMillis / 60_000;
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.values().removeIf(w -> w.minute() < minute);
        }
        // 같은 분이면 기존 카운터를 쓰고, 분이 바뀌었거나 처음이면 0 부터 다시 센다
        Window w = windows.compute(key, (k, old) ->
                old == null || old.minute() != minute ? new Window(minute, new AtomicInteger()) : old);
        return w.count().incrementAndGet() <= props.getRateLimitPerMinute();
    }
}
