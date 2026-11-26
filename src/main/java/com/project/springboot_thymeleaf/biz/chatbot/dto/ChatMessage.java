package com.project.springboot_thymeleaf.biz.chatbot.dto;

/** role: user | assistant (system 은 서버가 붙인다) */
public record ChatMessage(String role, String content) {
}
