package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * IF-CX-050 응모권 횟수 조회 요청 DTO
 * POST /api/v1/evm/ec-event-raffle-info
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteRaffleInfoReqDto {

    /** 이벤트 아이디 (PK, Number) */
    private Long eventId;

    /** 전자카드고객번호 (PK, String, 길이 10) */
    private String acntNo;

    /** 로그인 사용자 ID(OAuth2 email) */
    private String usrId;
}

