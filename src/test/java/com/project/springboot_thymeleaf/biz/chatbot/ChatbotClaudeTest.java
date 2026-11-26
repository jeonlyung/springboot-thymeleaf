package com.project.springboot_thymeleaf.biz.chatbot;

import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatbotMessageDto;
import com.project.springboot_thymeleaf.biz.chatbot.service.ChatbotService;
import com.project.springboot_thymeleaf.biz.chatbot.service.impl.ChatbotServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** provider=claude 경로: 실제 API 호출 없이 요청 구성과 키 미설정 처리만 검증한다. */
class ChatbotClaudeTest {

    private ChatbotServiceImpl svc(ChatbotProperties props) {
        return new ChatbotServiceImpl(props, new ObjectMapper(), WebClient.builder());
    }

    @Test
    void buildsClaudeRequestWithSystemPromptAndHistory() {
        ChatbotProperties props = new ChatbotProperties();
        props.setProvider("claude");
        props.setAnthropicApiKey("test-key");
        props.setAnthropicModel("claude-haiku-5-5");

        MessageCreateParams p = svc(props).buildClaudeParams(List.of(
                new ChatbotMessageDto("user", "안녕"),
                new ChatbotMessageDto("assistant", "반가워요"),
                new ChatbotMessageDto("user", "룰렛은 어떻게 해요?")));

        assertEquals(3, p.messages().size());
        assertTrue(p.system().isPresent(), "system 프롬프트는 별도 필드로 보내야 한다");
        assertEquals(props.getMaxTokens(), p.maxTokens());
    }

    @Test
    void failsFastWhenApiKeyMissing() {
        ChatbotProperties props = new ChatbotProperties();
        props.setProvider("claude");
        props.setAnthropicApiKey("");

        StepVerifier.create(svc(props).stream(List.of(new ChatbotMessageDto("user", "안녕"))))
                .expectError(ChatbotService.ChatUpstreamException.class)
                .verify();
    }
}
