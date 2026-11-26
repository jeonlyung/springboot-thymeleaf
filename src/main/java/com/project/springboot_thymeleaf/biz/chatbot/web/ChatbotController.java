package com.project.springboot_thymeleaf.biz.chatbot.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatbotMessageDto;
import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatbotChatReqDto;
import com.project.springboot_thymeleaf.biz.chatbot.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 챗봇 엔드포인트. 패키지에 ".api" 를 넣지 않는다(CustomInterceptor 가 Authorization 헤더를 요구하므로).
 *   GET  /chatbot/config  위젯 표시 여부
 *   POST /chatbot/chat    SSE: data:{"delta":"..."} ... data:[DONE] | event:error
 */
@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotProperties props;
    private final ChatbotService service;
    private final ObjectMapper mapper;

    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of("enabled", props.isEnabled(), "maxMessageLength", props.getMaxMessageLength());
    }

    @PostMapping("/chat")
    public ResponseEntity<Flux<ServerSentEvent<String>>> chat(@RequestBody ChatbotChatReqDto reqDto, HttpServletRequest http) {
        if (!props.isEnabled()) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "챗봇이 비활성화되어 있습니다.");
        }
        if (!service.checkRateLimit(http.getRemoteAddr())) {
            return error(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많아요. 잠시 후 다시 시도해 주세요.");
        }
        List<ChatbotMessageDto> history;
        try {
            history = service.sanitize(reqDto == null ? null : reqDto.getMessages());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        Flux<ServerSentEvent<String>> body = service.stream(history)
                .map(delta -> ServerSentEvent.<String>builder().data(json(Map.of("delta", delta))).build())
                .concatWith(Flux.just(ServerSentEvent.<String>builder().data("[DONE]").build()))
                .onErrorResume(e -> errorEvent("지금은 답변할 수 없어요. 잠시 후 다시 시도해 주세요."));
        return sse(HttpStatus.OK, body);
    }

    /** 오류도 같은 SSE 형식(event:error)으로 보내 클라이언트가 한 가지 파서만 쓰게 한다. */
    private ResponseEntity<Flux<ServerSentEvent<String>>> error(HttpStatus status, String msg) {
        return sse(status, errorEvent(msg));
    }

    private Flux<ServerSentEvent<String>> errorEvent(String msg) {
        return Flux.just(ServerSentEvent.<String>builder().event("error").data(json(Map.of("message", msg))).build());
    }

    private ResponseEntity<Flux<ServerSentEvent<String>>> sse(HttpStatus status, Flux<ServerSentEvent<String>> body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .header("Cache-Control", "no-cache")
                .header("X-Accel-Buffering", "no")   // nginx 버퍼링 방지
                .body(body);
    }

    private String json(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
