package com.project.springboot_thymeleaf.biz.chatbot.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.springboot_thymeleaf.biz.chatbot.config.ChatbotProperties;
import com.project.springboot_thymeleaf.biz.chatbot.dto.ChatbotMessageDto;
import com.project.springboot_thymeleaf.biz.chatbot.service.ChatbotService;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.ThinkingConfigDisabled;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 챗봇 답변을 만들어 주는 서비스.
 *
 * 브라우저가 보낸 대화 이력을 검증(sanitize)한 다음, LLM 에 보내고 답변을 글자 조각(delta) 단위로
 * 흘려보낸다. 컨트롤러는 이 조각을 SSE 로 그대로 브라우저에 내려준다.
 *
 * 어느 LLM 을 쓸지는 chatbot.provider 값으로 정한다. 코드는 안 바꾸고 환경변수(CHATBOT_PROVIDER)만 바꾸면 됨.
 *   openai : OpenAI 호환 서버. ai-serving 라우터, Ollama, vLLM 처럼 우리가 직접 띄우는 쪽 (기본값)
 *   claude : Anthropic API. 외부 LLM 이라 API 키(ANTHROPIC_API_KEY)가 필요하고 호출한 만큼 과금된다.
 *
 * 나중에 자체 구축 모델로 갈아탈 때는 provider 를 openai 로 돌리고 AI_SERVING_BASE_URL 만 맞추면 된다.
 * 그래서 openai 쪽 코드는 Claude 를 붙이면서도 지우지 않고 그대로 뒀다.
 *
 * 주의: 사용자 대화 내용은 로그에 남기지 않는다(개인정보). 로그에는 상태코드/예외 종류만 찍는다.
 */
@Slf4j
@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Set<String> ALLOWED_ROLES = Set.of("user", "assistant");

    private final ChatbotProperties props;
    private final ObjectMapper mapper;
    /** openai 호환 서버 호출용. provider 가 뭐든 만들어 두지만 claude 일 땐 안 쓴다. */
    private final WebClient client;
    /** Claude 호출용. provider=claude 이고 API 키가 있을 때만 만든다(그 외엔 null). */
    private final AnthropicClient claude;

    public ChatbotServiceImpl(ChatbotProperties props, ObjectMapper mapper, WebClient.Builder builder) {
        this.props = props;
        this.mapper = mapper;

        // 키가 없으면 클라이언트를 만들지 않는다. 앱 기동은 되게 하고, 실제로 질문이 들어왔을 때
        // streamClaude 에서 "지금은 답변할 수 없어요" 로 떨어지게 한다. (키 없다고 서버가 안 뜨면 곤란해서)
        this.claude = isClaude() && props.getAnthropicApiKey() != null && !props.getAnthropicApiKey().isBlank()
                ? AnthropicOkHttpClient.builder().apiKey(props.getAnthropicApiKey()).build()
                : null;

        // 아래는 기존 openai 호환 쪽 설정. 연결 타임아웃만 짧게(3초) 줘서 서버가 죽어 있을 때 빨리 실패하게 함
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

    /**
     * 브라우저가 보낸 대화 이력을 믿지 않고 한 번 걸러낸다. (이력은 브라우저 sessionStorage 에 있어서 조작 가능)
     *  - role 은 user / assistant 만 허용. 클라이언트가 system 을 끼워 넣어도 여기서 버려진다.
     *    (system 프롬프트는 서버가 직접 붙인다. 프롬프트 인젝션 방지)
     *  - 메시지 하나가 너무 길면 maxMessageLength 에서 자른다.
     *  - 최근 maxHistory 개만 남긴다. 토큰/비용 때문.
     *  - 첫 메시지는 user, 마지막 메시지도 user 여야 한다.
     * 조건을 못 맞추면 IllegalArgumentException → 컨트롤러가 400 으로 바꿔 준다.
     */
    @Override
    public List<ChatbotMessageDto> sanitize(List<ChatbotMessageDto> raw) {
        if (raw == null) {
            throw new IllegalArgumentException("messages 가 비어 있습니다.");
        }
        List<ChatbotMessageDto> out = new ArrayList<>();
        for (ChatbotMessageDto m : raw) {
            if (m == null || m.getRole() == null || m.getContent() == null) continue;
            String role = m.getRole().toLowerCase();
            String content = m.getContent().strip();
            if (!ALLOWED_ROLES.contains(role) || content.isEmpty()) continue;
            if (content.length() > props.getMaxMessageLength()) {
                content = content.substring(0, props.getMaxMessageLength());
            }
            out.add(new ChatbotMessageDto(role, content));
        }
        if (out.size() > props.getMaxHistory()) {
            out = new ArrayList<>(out.subList(out.size() - props.getMaxHistory(), out.size()));
        }
        // 첫 메시지는 user 여야 한다(일부 모델 템플릿이 assistant 시작을 거부)
        while (!out.isEmpty() && !"user".equals(out.get(0).getRole())) out.remove(0);
        if (out.isEmpty() || !"user".equals(out.get(out.size() - 1).getRole())) {
            throw new IllegalArgumentException("마지막 메시지는 사용자 메시지여야 합니다.");
        }
        return out;
    }

    /** openai 호환 요청 본문. system 프롬프트를 messages 맨 앞에 넣는 게 이쪽 형식이다. (Claude 는 따로 받음) */
    public Map<String, Object> buildBody(List<ChatbotMessageDto> history) {
        List<Map<String, String>> msgs = new ArrayList<>();
        msgs.add(Map.of("role", "system", "content", props.getSystemPrompt()));
        for (ChatbotMessageDto m : history) {
            msgs.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.getModel());
        body.put("messages", msgs);
        body.put("stream", true);
        body.put("max_tokens", props.getMaxTokens());
        body.put("temperature", props.getTemperature());
        return body;
    }

    private boolean isClaude() {
        return "claude".equalsIgnoreCase(props.getProvider());
    }

    /**
     * 답변 텍스트 조각(delta)을 순서대로 흘려주는 스트림. 컨트롤러가 이걸 구독해서 SSE 로 내려준다.
     * provider 만 보고 갈라지고, 어느 쪽이든 반환 형태(Flux&lt;String&gt;)가 같아서 컨트롤러/화면은 차이를 모른다.
     * 어느 쪽에서 실패하든 ChatUpstreamException 하나로 감싸서 올려 보낸다.
     */
    @Override
    public Flux<String> stream(List<ChatbotMessageDto> history) {
        return isClaude() ? streamClaude(history) : streamOpenAi(history);
    }

    /**
     * Claude 로 보낼 요청을 만든다. (실제 호출은 streamClaude)
     * openai 쪽이랑 다른 점:
     *  - system 프롬프트가 messages 안이 아니라 별도 필드(.system)다.
     *  - temperature 는 안 보낸다. Haiku 5.5 는 기본값 말고 다른 값을 넣으면 400 이 난다.
     *  - thinking 은 끈다. 룰렛/앱 사용법 안내 수준이라 깊게 생각할 필요가 없고, 켜 두면 느려지고
     *    max_tokens(1024) 를 생각하는 데 먼저 써버릴 수도 있다.
     */
    public MessageCreateParams buildClaudeParams(List<ChatbotMessageDto> history) {
        MessageCreateParams.Builder b = MessageCreateParams.builder()
                .model(props.getAnthropicModel())
                .maxTokens((long) props.getMaxTokens())
                .system(props.getSystemPrompt())
                .thinking(ThinkingConfigDisabled.builder().build());
        for (ChatbotMessageDto m : history) {
            if ("assistant".equals(m.getRole())) {
                b.addAssistantMessage(m.getContent());
            } else {
                b.addUserMessage(m.getContent());
            }
        }
        return b.build();
    }

    /**
     * Claude 호출. Anthropic SDK 의 스트림은 블로킹 방식(for 문으로 이벤트를 하나씩 꺼냄)이라
     * 그대로 쓰면 톰캣 요청 스레드를 붙잡는다. 그래서 boundedElastic 스레드에서 돌리고
     * Flux.create 로 감싸서 위쪽(컨트롤러)에는 논블로킹 스트림처럼 보이게 했다.
     */
    Flux<String> streamClaude(List<ChatbotMessageDto> history) {
        // 키가 없는 경우. 사용자 화면에는 평소 오류와 똑같은 문구가 나가고, 원인은 로그에서만 확인한다.
        if (claude == null) {
            log.warn("[ChatbotService] Claude 사용 설정이지만 ANTHROPIC_API_KEY 가 없습니다.");
            return Flux.error(new ChatUpstreamException(new IllegalStateException("ANTHROPIC_API_KEY 미설정")));
        }
        return Flux.<String>create(sink -> {
                    StreamResponse<RawMessageStreamEvent> res = claude.messages().createStreaming(buildClaudeParams(history));
                    // 사용자가 챗봇 창을 닫거나 새로고침해서 연결이 끊기면 Claude 쪽 스트림도 같이 닫는다.
                    // 안 닫으면 아무도 안 보는 답변을 끝까지 받느라 토큰(=돈)이 계속 나간다.
                    sink.onDispose(res::close);
                    try {
                        // 스트림에는 시작/종료/사용량 같은 이벤트도 섞여 온다. 글자가 들어 있는
                        // content_block_delta 만 골라서 text 를 내보낸다.
                        res.stream().forEach(ev -> ev.contentBlockDelta()
                                .flatMap(d -> d.delta().text())
                                .ifPresent(t -> sink.next(t.text())));
                        sink.complete();
                    } catch (Exception e) {
                        sink.error(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(props.getResponseTimeoutSeconds()))
                .onErrorMap(e -> {
                    // e.toString() 은 예외 종류/메시지만 나온다. 사용자가 입력한 내용은 안 들어간다.
                    log.warn("[ChatbotService] Claude 호출 실패: {}", e.toString());
                    return new ChatUpstreamException(e);
                });
    }

    /** OpenAI 호환 서버(ai-serving 등) 호출. 원래 있던 stream() 을 이름만 바꾼 것이다. */
    Flux<String> streamOpenAi(List<ChatbotMessageDto> history) {
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
                        log.warn("[ChatbotService] ai-serving 응답 오류 status={}", w.getStatusCode().value());
                    } else {
                        log.warn("[ChatbotService] ai-serving 호출 실패: {}", e.toString());
                    }
                    return new ChatUpstreamException(e);
                });
    }

    public String extractDelta(String data) {
        try {
            JsonNode content = mapper.readTree(data).path("choices").path(0).path("delta").path("content");
            return content.isTextual() ? content.asText() : null;
        } catch (Exception e) {
            return null;   // keep-alive 주석 등 JSON 이 아닌 조각은 무시
        }
    }
}
