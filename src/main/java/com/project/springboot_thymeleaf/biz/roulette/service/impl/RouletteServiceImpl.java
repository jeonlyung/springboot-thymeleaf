package com.project.springboot_thymeleaf.biz.roulette.service.impl;

import com.project.springboot_thymeleaf.biz.roulette.dto.*;
import com.project.springboot_thymeleaf.biz.roulette.service.RouletteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 룰렛 이벤트 서비스 구현체
 * 현재는 스텁(stub)으로 구성되어 있으며, 실제 외부 CXM 연동 시 WebClient 호출로 교체합니다.
 */
@Slf4j
@Service
public class RouletteServiceImpl implements RouletteService {

    // TODO: 실제 CXM API 연동 시 WebClient 빈 주입 및 호출 코드로 교체
    // private final WebClient cxmWebClient;

    /**
     * IF-CX-047 이벤트 정보 조회
     * 실제 연동 전까지 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteEventListResDto getEventList() {
        log.info("[RouletteService] getEventList");

        // TODO: 실제 CXM API 호출로 교체
        // return cxmWebClient.post()
        //     .uri("/api/v1/evm/ec-event-list")
        //     .retrieve()
        //     .bodyToMono(RouletteEventListResDto.class)
        //     .block();

        // 스텁: 진행 중인 이벤트 1건 응답
        return RouletteEventListResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteEventListResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventList(List.of(
                                RouletteEventListResDto.EcEvent.builder()
                                        .eventId(1L)
                                        .eventName("봄맞이 룰렛 이벤트")
                                        .eventStartDate("20260301")
                                        .eventEndDate("20260430")
                                        .build()
                        ))
                        .build())
                .build();
    }

    /**
     * IF-CX-049 이벤트 혜택 구성 정보 조회
     * 실제 연동 전까지 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteEventPrizeListResDto getEventPrizeList(RouletteEventPrizeListReqDto reqDto) {
        log.info("[RouletteService] getEventPrizeList - eventId={}", reqDto.getEventId());

        // TODO: 실제 CXM API 호출로 교체
        // return cxmWebClient.post()
        //     .uri("/api/v1/evm/ec-event-prize-list")
        //     .bodyValue(reqDto)
        //     .retrieve()
        //     .bodyToMono(RouletteEventPrizeListResDto.class)
        //     .block();

        // 스텁: 경품 목록 6개 응답
        return RouletteEventPrizeListResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteEventPrizeListResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventPrizeList(List.of(
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("10% 할인 쿠폰").couponCode("DC10").winningRate(20).build(),
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("무료 배송").couponCode("FREE_SHIP").winningRate(20).build(),
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("5,000 포인트").couponCode("PT5000").winningRate(15).build(),
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("20% 할인 쿠폰").couponCode("DC20").winningRate(10).build(),
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("1,000 포인트").couponCode("PT1000").winningRate(15).build(),
                                RouletteEventPrizeListResDto.EcEventPrize.builder()
                                        .couponName("꽝").couponCode("NONE").winningRate(20).build()
                        ))
                        .build())
                .build();
    }

    /**
     * IF-CX-050 응모권 횟수 조회
     * 실제 연동 전까지 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteRaffleInfoResDto getRaffleInfo(RouletteRaffleInfoReqDto reqDto) {
        log.info("[RouletteService] getRaffleInfo - eventId={}, acntNo={}", reqDto.getEventId(), reqDto.getAcntNo());

        // TODO: 실제 CXM API 호출로 교체
        // return cxmWebClient.get()
        //     .uri("/api/v1/evm/ec-event-raffle-info")
        //     .retrieve()
        //     .bodyToMono(RouletteRaffleInfoResDto.class)
        //     .block();

        // 스텁: 응모권 3장 보유 응답
        return RouletteRaffleInfoResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteRaffleInfoResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventRaffleInfo(List.of(
                                RouletteRaffleInfoResDto.EcEventRaffleInfo.builder()
                                        .raffleTicketCount(3)
                                        .build()
                        ))
                        .build())
                .build();
    }

    /**
     * IF-CX-051 당첨자 선정 (룰렛 스핀)
     * 실제 연동 전까지 랜덤 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteDrawResDto drawEvent(RouletteDrawReqDto reqDto) {
        log.info("[RouletteService] drawEvent - eventId={}, acntNo={}", reqDto.getEventId(), reqDto.getAcntNo());

        // TODO: 실제 CXM API 호출로 교체
        // return cxmWebClient.post()
        //     .uri("/api/v1/evm/ec-event-draw")
        //     .bodyValue(reqDto)
        //     .retrieve()
        //     .bodyToMono(RouletteDrawResDto.class)
        //     .block();

        // 스텁: 랜덤 당첨 시뮬레이션
        String[] coupons = { "10% 할인 쿠폰", "무료 배송", "5,000 포인트", "20% 할인 쿠폰", "1,000 포인트", "꽝" };
        int randomIdx = (int) (Math.random() * coupons.length);
        String picked = coupons[randomIdx];
        boolean isWin = !picked.equals("꽝");

        return RouletteDrawResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteDrawResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventDraw(List.of(
                                RouletteDrawResDto.EcEventDraw.builder()
                                        .isWinning(isWin ? "1" : "0")
                                        .winningCouponName(isWin ? picked : null)
                                        .build()
                        ))
                        .build())
                .build();
    }

    /**
     * IF-CX-052 이벤트 신청대상 확인
     * 실제 연동 전까지 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteTargetCheckResDto checkEventTarget(RouletteTargetCheckReqDto reqDto) {
        log.info("[RouletteService] checkEventTarget - eventId={}, acntNo={}", reqDto.getEventId(), reqDto.getAcntNo());

        // TODO: 실제 CXM API 호출로 교체
        // return cxmWebClient.post()
        //     .uri("/api/v1/evm/ec-event-target-check")
        //     .bodyValue(reqDto)
        //     .retrieve()
        //     .bodyToMono(RouletteTargetCheckResDto.class)
        //     .block();

        // 스텁: 대상자(isTarget=1) 응답
        return RouletteTargetCheckResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteTargetCheckResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventTargetCheck(List.of(
                                RouletteTargetCheckResDto.EcEventTargetCheck.builder()
                                        .isTarget("1")
                                        .targetCouponName(null)
                                        .build()
                        ))
                        .build())
                .build();
    }
}

