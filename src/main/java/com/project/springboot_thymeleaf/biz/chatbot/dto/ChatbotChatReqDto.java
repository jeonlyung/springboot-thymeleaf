package com.project.springboot_thymeleaf.biz.chatbot.dto;

import java.util.List;

/** 브라우저 → 서버. 대화 이력은 클라이언트가 들고 있고 매 요청마다 보낸다(서버 무상태). */
public record ChatRequest(List<ChatMessage> messages) {
}
