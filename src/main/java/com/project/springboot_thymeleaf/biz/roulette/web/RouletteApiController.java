package com.project.springboot_thymeleaf.biz.roulette.web;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import com.project.springboot_thymeleaf.biz.roulette.dto.*;
import com.project.springboot_thymeleaf.biz.roulette.service.RouletteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 룰렛 이벤트 REST API 컨트롤러
 *
 * (init) GET  /api/roulette/init           - 페이지 초기화 (acntNo, eventId 서버 반환)
 * IF-CX-050   GET  /api/roulette/raffle-info - 응모권 횟수 조회
 * IF-CX-051   POST /api/roulette/draw        - 당첨자 선정(스핀)
 * IF-CX-052   POST /api/roulette/target-check - 이벤트 신청대상 확인
 *
 * IF-CX-047 / IF-CX-049 는 RouletteEvmApiController (/api/v1/evm) 에서 처리합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roulette")
public class RouletteApiController {

    private final RouletteService rouletteService;

    /**
     * 페이지 초기화 - acntNo / eventId 를 서버에서 반환
     * 프론트 → GET /api/roulette/init
     */
    @GetMapping("/init")
    public ResponseEntity<RoulettePageInitResDto> getPageInit(
            @AuthenticationPrincipal Object principal) {
        String usrId = "";
        if (principal instanceof CustomOAuth2User oauthUser) {
            usrId = oauthUser.getEmail();
        }
        return ResponseEntity.ok(rouletteService.getPageInit(usrId));
    }

    /**
     * IF-CX-050 응모권 횟수 조회
     * 프론트 → GET /api/roulette/raffle-info?eventId={id}&acntNo={no}
     */
    @GetMapping("/raffle-info")
    public ResponseEntity<RouletteRaffleInfoResDto> getRaffleInfo(
            @RequestParam Long eventId,
            @RequestParam String acntNo) {

        RouletteRaffleInfoReqDto reqDto = RouletteRaffleInfoReqDto.builder()
                .eventId(eventId)
                .acntNo(acntNo)
                .build();

        return ResponseEntity.ok(rouletteService.getRaffleInfo(reqDto));
    }

    /**
     * IF-CX-051 당첨자 선정 (룰렛 스핀)
     * 프론트 → POST /api/roulette/draw
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/draw")
    public ResponseEntity<RouletteDrawResDto> draw(@RequestBody RouletteDrawReqDto reqDto) {
        return ResponseEntity.ok(rouletteService.drawEvent(reqDto));
    }

    /**
     * IF-CX-052 이벤트 신청대상 확인
     * 프론트 → POST /api/roulette/target-check
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/target-check")
    public ResponseEntity<RouletteTargetCheckResDto> targetCheck(@RequestBody RouletteTargetCheckReqDto reqDto) {
        return ResponseEntity.ok(rouletteService.checkEventTarget(reqDto));
    }
}

