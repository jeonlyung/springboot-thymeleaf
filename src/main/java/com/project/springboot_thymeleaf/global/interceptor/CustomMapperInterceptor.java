package com.project.springboot_thymeleaf.global.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Properties;

// 1. StatementHandler의 prepare 메서드를 가로챕니다 (SQL이 생성되는 시점)
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
@Component
@Slf4j
public class CustomMapperInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 실행 시간 측정을 위해 시작 시간 기록
        long startTime = System.currentTimeMillis();

        // 실제 쿼리 실행 객체 가져오기
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        String sql = handler.getBoundSql().getSql();

        try {
            // 2. 실제 쿼리 실행 (원래 가려던 길로 보내주기)
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 3. 로그 출력 (아까 2만 건 인서트처럼 너무 크면 안 찍히게 조절 가능)
            if (sql.length() < 1000) {
                log.info("SQL Execution Time: {}ms | Query: {}", duration, sql.replaceAll("\\s+", " "));
            } else {
                log.info("Large SQL Executed. Length: {} | Time: {}ms", sql.length(), duration);
            }
        }
    }
}