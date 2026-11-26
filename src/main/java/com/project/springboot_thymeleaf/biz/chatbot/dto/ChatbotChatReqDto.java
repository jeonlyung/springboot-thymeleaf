package com.project.springboot_thymeleaf.biz.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 챗봇 대화 요청 DTO
 * POST /chatbot/chat
 * 대화 이력은 브라우저(sessionStorage)가 들고 있다가 매 요청마다 같이 보낸다. 서버는 이력을 저장하지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotChatReqDto {

    /** 대화 이력 (마지막 항목이 이번 질문) */
    private List<ChatbotMessageDto> messages;
}
