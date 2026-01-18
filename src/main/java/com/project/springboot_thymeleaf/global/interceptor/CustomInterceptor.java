package com.project.springboot_thymeleaf.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Slf4j
@Component
public class CustomInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 공통 로직 (모든 비즈니스 로직이 거치는 곳)
        log.debug(">>> [Common Business Logic] URI: {}", request.getRequestURI());

        // 2. 패키지 기반 선별 로직 (API 패키지일 경우만 헤더/파라미터 검증)
        if (handler instanceof HandlerMethod handlerMethod) {
            // 현재 요청을 처리할 컨트롤러의 패키지 명 추출
            String packageName = handlerMethod.getBeanType().getPackageName();

            // 'api' 패키지에 포함된 컨트롤러인 경우만 실행
            if (packageName.contains(".api")) {
                log.debug(">>> [API Special Check] Target Package: {}", packageName);

                // [헤더 검증]
                String auth = request.getHeader("Authorization");
                if (auth == null || auth.isBlank()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API 인증 실패");
                    return false;
                }

                // [파라미터 검증]
                if (request.getParameterMap().isEmpty()) {
                    log.warn("API 호출 시 파라미터가 없습니다.");
                }
            }
        }

        return true;

    }
}