package com.project.springboot_thymeleaf.biz.chatbot.service;

import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatbotMessageDto;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 챗봇 서비스 인터페이스
 * 대화 이력을 검증하고, LLM 에 질문을 보내 답변을 글자 조각 단위로 받아온다.
 * 어떤 LLM 에 붙을지는 구현체에서 chatbot.provider 설정으로 정한다. (openai 호환 서버 / claude)
 */
public interface ChatbotService {

    /**
     * 클라이언트가 보낸 대화 이력 검증/정리
     * POST /chatbot/chat 요청 처리 전에 호출
     *
     * @throws IllegalArgumentException 이력이 비었거나 마지막 메시지가 user 가 아닐 때 (컨트롤러가 400 으로 변환)
     */
    List<ChatbotMessageDto> sanitize(List<ChatbotMessageDto> raw);

    /**
     * 질문을 LLM 에 보내고 답변 텍스트 조각(delta)을 순서대로 스트리밍
     * POST /chatbot/chat
     * 업스트림 오류는 {@link ChatUpstreamException} 으로 변환되어 흐른다.
     */
    Flux<String> stream(List<ChatbotMessageDto> history);

    /** LLM 서버(ai-serving / Claude) 호출 실패. 원인 예외는 cause 에 담긴다. */
    class ChatUpstreamException extends RuntimeException {
        public ChatUpstreamException(Throwable cause) {
            super("AI 서버 호출 실패", cause);
        }
    }
}
