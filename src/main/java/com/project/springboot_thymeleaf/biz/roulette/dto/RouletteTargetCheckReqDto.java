package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * IF-CX-052 이벤트 신청대상 확인 요청 DTO
 * POST /api/v1/evm/ec-event-target-check
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteTargetCheckReqDto {

    /** 이벤트 아이디 (PK, Number) */
    private Long eventId;

    /** 전자카드고객번호 (PK, String, 길이 10) */
    private String acntNo;
}

