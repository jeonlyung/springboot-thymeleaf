package com.project.springboot_thymeleaf.biz.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 챗봇 대화 메시지 1건 DTO
 * 요청(이력)과 서비스 내부에서 같이 쓴다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessageDto {

    /** 발화자 (user | assistant). system 은 서버가 직접 붙이기 때문에 클라이언트가 보내도 버린다 */
    private String role;

    /** 메시지 내용 */
    private String content;
}
