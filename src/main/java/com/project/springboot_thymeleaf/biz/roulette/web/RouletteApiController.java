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
 * (init) POST /api/v1/roulette/init            - 페이지 초기화 (acntNo, eventId 서버 반환)
 * IF-CX-050   POST /api/v1/roulette/raffle-info - 응모권 횟수 조회
 * (custom)    POST /api/v1/roulette/raffle-join - 응모권 참여(+1)
 * IF-CX-051   POST /api/v1/roulette/draw        - 당첨자 선정(스핀)
 * IF-CX-052   POST /api/v1/roulette/target-check - 이벤트 신청대상 확인
 *
 * IF-CX-047 / IF-CX-049 는 RouletteEvmApiController (/api/v1/evm) 에서 처리합니다.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/roulette", "/api/v1/roulette"})
public class RouletteApiController {

    private final RouletteService rouletteService;

    /**
     * 페이지 초기화 - acntNo / eventId 를 서버에서 반환
     * 프론트 → POST /api/v1/roulette/init
     */
    @PostMapping("/init")
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
     * 프론트 → POST /api/v1/roulette/raffle-info
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/raffle-info")
    public ResponseEntity<RouletteRaffleInfoResDto> getRaffleInfo(
            @RequestBody RouletteRaffleInfoReqDto reqDto,
            @AuthenticationPrincipal Object principal) {
        resolveRequestContext(reqDto, principal);
        return ResponseEntity.ok(rouletteService.getRaffleInfo(reqDto));
    }

    /**
     * 응모권 참여(+1)
     * 프론트 → POST /api/v1/roulette/raffle-join
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/raffle-join")
    public ResponseEntity<RouletteRaffleInfoResDto> joinRaffle(
            @RequestBody RouletteRaffleInfoReqDto reqDto,
            @AuthenticationPrincipal Object principal) {
        resolveRequestContext(reqDto, principal);
        return ResponseEntity.ok(rouletteService.joinRaffle(reqDto));
    }

    /**
     * IF-CX-051 당첨자 선정 (룰렛 스핀)
     * 프론트 → POST /api/v1/roulette/draw
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/draw")
    public ResponseEntity<RouletteDrawResDto> draw(
            @RequestBody RouletteDrawReqDto reqDto,
            @AuthenticationPrincipal Object principal) {
        resolveRequestContext(reqDto, principal);
        return ResponseEntity.ok(rouletteService.drawEvent(reqDto));
    }

    /**
     * IF-CX-052 이벤트 신청대상 확인
     * 프론트 → POST /api/v1/roulette/target-check
     * Body: { "eventId": 1, "acntNo": "1234567890" }
     */
    @PostMapping("/target-check")
    public ResponseEntity<RouletteTargetCheckResDto> targetCheck(
            @RequestBody RouletteTargetCheckReqDto reqDto,
            @AuthenticationPrincipal Object principal) {
        resolveRequestContext(reqDto, principal);
        return ResponseEntity.ok(rouletteService.checkEventTarget(reqDto));
    }

    private void resolveRequestContext(RouletteRaffleInfoReqDto reqDto, Object principal) {
        String usrId = extractUsrId(principal);
        if (usrId.isBlank()) {
            return;
        }
        reqDto.setUsrId(usrId);
        if (reqDto.getEventId() != null) {
            return;
        }
        RoulettePageInitResDto init = rouletteService.getPageInit(usrId);
        reqDto.setEventId(init.getEventId());
    }

    private void resolveRequestContext(RouletteDrawReqDto reqDto, Object principal) {
        String usrId = extractUsrId(principal);
        if (usrId.isBlank()) {
            return;
        }
        reqDto.setUsrId(usrId);
        if (reqDto.getEventId() != null) {
            return;
        }
        RoulettePageInitResDto init = rouletteService.getPageInit(usrId);
        reqDto.setEventId(init.getEventId());
    }

    private void resolveRequestContext(RouletteTargetCheckReqDto reqDto, Object principal) {
        String usrId = extractUsrId(principal);
        if (usrId.isBlank()) {
            return;
        }
        reqDto.setUsrId(usrId);
        if (reqDto.getEventId() != null) {
            return;
        }
        RoulettePageInitResDto init = rouletteService.getPageInit(usrId);
        reqDto.setEventId(init.getEventId());
    }

    private String extractUsrId(Object principal) {
        if (principal instanceof CustomOAuth2User oauthUser && oauthUser.getEmail() != null) {
            return oauthUser.getEmail();
        }
        return "";
    }
}

