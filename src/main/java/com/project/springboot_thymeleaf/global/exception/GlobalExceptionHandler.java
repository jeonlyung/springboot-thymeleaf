package com.project.springboot_thymeleaf.global.exception;

import jakarta.security.auth.message.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(Exception e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("권한 에러 발생: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // 모든 시스템 예외를 여기서 공통으로 처리
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("공통 에러 발생: " + e.getMessage());
    }
}
