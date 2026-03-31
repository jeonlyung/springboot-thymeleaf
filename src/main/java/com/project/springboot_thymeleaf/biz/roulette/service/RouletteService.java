package com.project.springboot_thymeleaf.biz.roulette.service;

import com.project.springboot_thymeleaf.biz.roulette.dto.*;

/**
 * 룰렛 이벤트 서비스 인터페이스
 * IF-CX-050, IF-CX-051, IF-CX-052 대응
 */
public interface RouletteService {

    /**
     * IF-CX-050 응모권 횟수 조회
     * GET /api/v1/evm/ec-event-raffle-info
     */
    RouletteRaffleInfoResDto getRaffleInfo(RouletteRaffleInfoReqDto reqDto);

    /**
     * IF-CX-051 당첨자 선정 (룰렛 스핀)
     * POST /api/v1/evm/ec-event-draw
     */
    RouletteDrawResDto drawEvent(RouletteDrawReqDto reqDto);

    /**
     * IF-CX-052 이벤트 신청대상 확인
     * POST /api/v1/evm/ec-event-target-check
     */
    RouletteTargetCheckResDto checkEventTarget(RouletteTargetCheckReqDto reqDto);
}

