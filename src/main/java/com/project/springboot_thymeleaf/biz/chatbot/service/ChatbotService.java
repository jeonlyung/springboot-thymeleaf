package com.project.springboot_thymeleaf.biz.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatMessage;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ai-serving 라우터(OpenAI 호환)를 호출해 답변 토큰 스트림을 돌려준다.
 * 사용자 대화 내용은 로그에 남기지 않는다(개인정보).
 */
@Slf4j
@Service
public class ChatbotService {

    private static final Set<String> ALLOWED_ROLES = Set.of("user", "assistant");

    private final ChatbotProperties props;
    private final ObjectMapper mapper;
    private final WebClient client;

    public ChatbotService(ChatbotProperties props, ObjectMapper mapper, WebClient.Builder builder) {
        this.props = props;
        this.mapper = mapper;
        HttpClient http = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs());
        WebClient.Builder b = builder
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(http));
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey());
        }
        this.client = b.build();
    }

    /** 클라이언트가 보낸 이력을 검증·정리: role 화이트리스트, 길이 제한, 최근 N개, 마지막은 user 여야 함. */
    public List<ChatMessage> sanitize(List<ChatMessage> raw) {
        if (raw == null) {
            throw new IllegalArgumentException("messages 가 비어 있습니다.");
        }
        List<ChatMessage> out = new ArrayList<>();
        for (ChatMessage m : raw) {
            if (m == null || m.role() == null || m.content() == null) continue;
            String role = m.role().toLowerCase();
            String content = m.content().strip();
            if (!ALLOWED_ROLES.contains(role) || content.isEmpty()) continue;
            if (content.length() > props.getMaxMessageLength()) {
                content = content.substring(0, props.getMaxMessageLength());
            }
            out.add(new ChatMessage(role, content));
        }
        if (out.size() > props.getMaxHistory()) {
            out = new ArrayList<>(out.subList(out.size() - props.getMaxHistory(), out.size()));
        }
        // 첫 메시지는 user 여야 한다(일부 모델 템플릿이 assistant 시작을 거부)
        while (!out.isEmpty() && !"user".equals(out.get(0).role())) out.remove(0);
        if (out.isEmpty() || !"user".equals(out.get(out.size() - 1).role())) {
            throw new IllegalArgumentException("마지막 메시지는 사용자 메시지여야 합니다.");
        }
        return out;
    }

    Map<String, Object> buildBody(List<ChatMessage> history) {
        List<Map<String, String>> msgs = new ArrayList<>();
        msgs.add(Map.of("role", "system", "content", props.getSystemPrompt()));
        for (ChatMessage m : history) {
            msgs.add(Map.of("role", m.role(), "content", m.content()));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.getModel());
        body.put("messages", msgs);
        body.put("stream", true);
        body.put("max_tokens", props.getMaxTokens());
        body.put("temperature", props.getTemperature());
        return body;
    }

    /** 답변 텍스트 조각(delta) 스트림. 업스트림 오류는 ChatUpstreamException 으로 변환된다. */
    public Flux<String> stream(List<ChatMessage> history) {
        return client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(buildBody(history))
                .retrieve()
                .bodyToFlux(String.class)           // text/event-stream → data 페이로드 단위
                .timeout(Duration.ofSeconds(props.getResponseTimeoutSeconds()))
                .takeWhile(data -> !"[DONE]".equals(data.strip()))
                .mapNotNull(this::extractDelta)
                .filter(s -> !s.isEmpty())
                .onErrorMap(e -> {
                    if (e instanceof WebClientResponseException w) {
                        log.warn("ai-serving 응답 오류 status={}", w.getStatusCode().value());
                    } else {
                        log.warn("ai-serving 호출 실패: {}", e.toString());
                    }
                    return new ChatUpstreamException(e);
                });
    }

    String extractDelta(String data) {
        try {
            JsonNode content = mapper.readTree(data).path("choices").path(0).path("delta").path("content");
            return content.isTextual() ? content.asText() : null;
        } catch (Exception e) {
            return null;   // keep-alive 주석 등 JSON 이 아닌 조각은 무시
        }
    }

    public static class ChatUpstreamException extends RuntimeException {
        public ChatUpstreamException(Throwable cause) {
            super("AI 서버 호출 실패", cause);
        }
    }
}
