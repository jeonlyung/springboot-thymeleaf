package com.project.springboot_thymeleaf.biz.roulette.web;

import com.project.springboot_thymeleaf.biz.roulette.dto.RouletteEventListResDto;
import com.project.springboot_thymeleaf.biz.roulette.dto.RouletteEventPrizeListReqDto;
import com.project.springboot_thymeleaf.biz.roulette.dto.RouletteEventPrizeListResDto;
import com.project.springboot_thymeleaf.biz.roulette.service.RouletteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 룰렛 이벤트 EVM REST API 컨트롤러
 * 프론트엔드 API_CONFIG 의 eventListUrl / prizeListUrl 과 매핑
 *
 * IF-CX-047  POST /api/v1/evm/ec-event-list        - 이벤트 정보 조회
 * IF-CX-049  POST /api/v1/evm/ec-event-prize-list  - 이벤트 혜택 구성 정보 조회
 *
 * 실제 CXM 연동 시 RouletteServiceImpl 의 스텁 코드를 WebClient 호출로 교체합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/evm")
public class RouletteEvmApiController {

    private final RouletteService rouletteService;

    /**
     * IF-CX-047 이벤트 정보 조회
     * 프론트 → POST /api/v1/evm/ec-event-list
     */
    @PostMapping("/ec-event-list")
    public ResponseEntity<RouletteEventListResDto> getEventList() {
        return ResponseEntity.ok(rouletteService.getEventList());
    }

    /**
     * IF-CX-049 이벤트 혜택 구성 정보 조회
     * 프론트 → POST /api/v1/evm/ec-event-prize-list
     * Body: { "eventId": 1 }
     */
    @PostMapping("/ec-event-prize-list")
    public ResponseEntity<RouletteEventPrizeListResDto> getEventPrizeList(
            @RequestBody RouletteEventPrizeListReqDto reqDto) {
        return ResponseEntity.ok(rouletteService.getEventPrizeList(reqDto));
    }
}

