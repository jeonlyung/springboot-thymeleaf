package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

/**
 * IF-CX-049 이벤트 혜택 구성 정보 조회 요청 DTO
 * POST /api/v1/evm/ec-event-prize-list
 * Request: { eventId }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteEventPrizeListReqDto {

    /** 이벤트 아이디 (PK, Number) */
    private Long eventId;
}

