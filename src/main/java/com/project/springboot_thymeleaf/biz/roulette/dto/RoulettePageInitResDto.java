package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

/**
 * 룰렛 페이지 초기화 응답 DTO
 * GET /api/roulette/init
 *
 * 프론트엔드 API_CONFIG 의 acntNo / eventId 초기값을 서버에서 주입합니다.
 * - acntNo : 현재 로그인 사용자의 전자카드고객번호 (실제 연동 전 서버 stub 반환)
 * - eventId: 현재 진행 중인 이벤트 ID (loadEventDefinitions 에서 업데이트됨)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoulettePageInitResDto {

    /** 전자카드 고객번호 (String, 길이 10) */
    private String acntNo;

    /** 초기 이벤트 아이디 (loadEventDefinitions 호출 후 실제값으로 교체됨) */
    private Long eventId;
}

