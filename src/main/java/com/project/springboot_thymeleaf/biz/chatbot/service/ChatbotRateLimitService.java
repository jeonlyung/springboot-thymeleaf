package com.project.springboot_thymeleaf.biz.chatbot.service;

/**
 * 챗봇 호출 횟수 제한 서비스 인터페이스
 * 챗봇은 로그인 없이(permitAll) 쓸 수 있어서, 막 호출해서 LLM 비용이 나가지 않게 IP 별로 막는다.
 */
public interface ChatbotRateLimitService {

    /**
     * 호출 허용 여부 확인 (확인하는 순간 횟수도 1 증가)
     *
     * @param key 제한 단위 (현재는 접속 IP)
     * @return true 면 허용, false 면 분당 한도 초과
     */
    boolean tryAcquire(String key);
}
