package com.project.springboot_thymeleaf.biz.roulette.service.impl;

import com.project.springboot_thymeleaf.biz.roulette.dto.*;
import com.project.springboot_thymeleaf.biz.roulette.mapper.RouletteMapper;
import com.project.springboot_thymeleaf.biz.roulette.service.RouletteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 룰렛 이벤트 서비스 구현체
 * 현재는 스텁(stub)으로 구성되어 있으며, 실제 외부 CXM 연동 시 WebClient 호출로 교체합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteServiceImpl implements RouletteService {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    private final RouletteMapper rouletteMapper;
    private final Random random = new Random();

    /**
     * 페이지 초기화 - acntNo / eventId 반환
     * acntNo: 실제 CXM 연동 전, 서버 고정값 사용
     *         연동 후에는 usrId 로 CXM DB 조회해서 반환
     */
    @Override
    public RoulettePageInitResDto getPageInit(String usrId) {
        log.info("[RouletteService] getPageInit - usrId={}", usrId);

        RouletteEventDbDto currentEvent = findCurrentEvent();
        Long eventId = currentEvent != null ? currentEvent.getEventId() : null;

        String acntNo = null;
        if (eventId != null && usrId != null && !usrId.isBlank()) {
            acntNo = rouletteMapper.selectAcntNoByUsrIdAndEventId(usrId, eventId);
        }
        if ((acntNo == null || acntNo.isBlank()) && usrId != null && !usrId.isBlank()) {
            acntNo = rouletteMapper.selectAnyAcntNoByUsrId(usrId);
        }
        if (acntNo == null || acntNo.isBlank()) {
            // 대상 데이터 미매핑 환경에서도 룰렛을 테스트할 수 있도록 기본 고객번호를 사용합니다.
            acntNo = "1234567890";
        }

        return RoulettePageInitResDto.builder()
                .acntNo(acntNo != null ? acntNo : "")
                .eventId(eventId)
                .build();
    }

    /**
     * IF-CX-047 이벤트 정보 조회
     * 실제 연동 전까지 스텁 응답을 반환합니다.
     */
    @Override
    public RouletteEventListResDto getEventList() {
        log.info("[RouletteService] getEventList");

        List<RouletteEventDbDto> eventRows = rouletteMapper.selectEventList();
        List<RouletteEventListResDto.EcEvent> eventList = new ArrayList<>();

        for (RouletteEventDbDto row : eventRows) {
            eventList.add(RouletteEventListResDto.EcEvent.builder()
                    .eventId(row.getEventId())
                    .eventName(row.getEventName())
                    .eventStartDate(row.getEventStartDate())
                    .eventEndDate(row.getEventEndDate())
                    .dispContents(row.getDispContents())
                    .build());
        }

        return RouletteEventListResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteEventListResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventList(eventList)
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

        List<RoulettePrizeDbDto> prizeRows = rouletteMapper.selectPrizeListByEventId(reqDto.getEventId());
        List<RouletteEventPrizeListResDto.EcEventPrize> prizeList = new ArrayList<>();

        for (RoulettePrizeDbDto row : prizeRows) {
            prizeList.add(RouletteEventPrizeListResDto.EcEventPrize.builder()
                    .couponName(row.getCouponName())
                    .couponCode(row.getCouponCode())
                    .winningRate(row.getWinningRate())
                    .build());
        }

        return RouletteEventPrizeListResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteEventPrizeListResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventPrizeList(prizeList)
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

        ensureDefaultTargetAndBalance(reqDto.getEventId(), reqDto.getAcntNo());
        Integer ticketCount = rouletteMapper.selectRaffleTicketCount(reqDto.getEventId(), reqDto.getAcntNo());
        int safeTicketCount = Math.max(ticketCount != null ? ticketCount : 0, 0);

        return RouletteRaffleInfoResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteRaffleInfoResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventRaffleInfo(List.of(
                                RouletteRaffleInfoResDto.EcEventRaffleInfo.builder()
                                        .raffleTicketCount(safeTicketCount)
                                        .build()
                        ))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public RouletteRaffleInfoResDto joinRaffle(RouletteRaffleInfoReqDto reqDto) {
        log.info("[RouletteService] joinRaffle - eventId={}, acntNo={}", reqDto.getEventId(), reqDto.getAcntNo());

        ensureDefaultTargetAndBalance(reqDto.getEventId(), reqDto.getAcntNo());
        rouletteMapper.incrementRaffleTicketCount(reqDto.getEventId(), reqDto.getAcntNo());

        Integer ticketCount = rouletteMapper.selectRaffleTicketCount(reqDto.getEventId(), reqDto.getAcntNo());
        int safeTicketCount = Math.max(ticketCount != null ? ticketCount : 0, 0);

        return RouletteRaffleInfoResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteRaffleInfoResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventRaffleInfo(List.of(
                                RouletteRaffleInfoResDto.EcEventRaffleInfo.builder()
                                        .raffleTicketCount(safeTicketCount)
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
    @Transactional
    public RouletteDrawResDto drawEvent(RouletteDrawReqDto reqDto) {
        log.info("[RouletteService] drawEvent - eventId={}, acntNo={}", reqDto.getEventId(), reqDto.getAcntNo());

        Long eventId = reqDto.getEventId();
        String acntNo = reqDto.getAcntNo();

        ensureDefaultTargetAndBalance(eventId, acntNo);
        String targetYn = rouletteMapper.selectTargetYn(eventId, acntNo);
        if (!"Y".equalsIgnoreCase(targetYn)) {
            return buildDrawFailure("참여 대상이 아닙니다.");
        }

        Integer currentTicket = rouletteMapper.selectRaffleTicketCount(eventId, acntNo);
        if (currentTicket == null || currentTicket <= 0) {
            return buildDrawFailure("응모권이 없습니다.");
        }

        List<RoulettePrizeDbDto> prizeRows = rouletteMapper.selectPrizeListByEventId(eventId);
        if (prizeRows.isEmpty()) {
            return buildDrawFailure("경품 구성이 없습니다.");
        }

        RoulettePrizeDbDto pickedPrize = pickPrizeByWeight(prizeRows);
        boolean isWin = "Y".equalsIgnoreCase(pickedPrize.getWinningPrizeYn());

        int affected = rouletteMapper.decrementRaffleTicketCount(eventId, acntNo);
        if (affected <= 0) {
            return buildDrawFailure("응모권 차감에 실패했습니다.");
        }

        rouletteMapper.insertDrawHistory(RouletteDrawHistoryDbDto.builder()
                .eventId(eventId)
                .acntNo(acntNo)
                .drawResultCode(isWin ? "1" : "0")
                .winningCouponName(isWin ? pickedPrize.getCouponName() : null)
                .winningCouponCode(isWin ? pickedPrize.getCouponCode() : null)
                .regId("SYSTEM")
                .build());

        return RouletteDrawResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteDrawResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventDraw(List.of(
                                RouletteDrawResDto.EcEventDraw.builder()
                                        .isWinning(isWin ? "1" : "0")
                                        .winningCouponName(isWin ? pickedPrize.getCouponName() : null)
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

        ensureDefaultTargetAndBalance(reqDto.getEventId(), reqDto.getAcntNo());
        String targetYn = rouletteMapper.selectTargetYn(reqDto.getEventId(), reqDto.getAcntNo());
        boolean isTarget = "Y".equalsIgnoreCase(targetYn);
        String targetCouponName = isTarget
                ? rouletteMapper.selectTargetCouponName(reqDto.getEventId(), reqDto.getAcntNo())
                : null;

        return RouletteTargetCheckResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteTargetCheckResDto.ResultData.builder()
                        .isSuccess(true)
                        .errMsg(null)
                        .ecEventTargetCheck(List.of(
                                RouletteTargetCheckResDto.EcEventTargetCheck.builder()
                                        .isTarget(isTarget ? "1" : "0")
                                        .targetCouponName(targetCouponName)
                                        .build()
                        ))
                        .build())
                .build();
    }

    private RouletteEventDbDto findCurrentEvent() {
        String todayYmd = LocalDate.now().format(YYYYMMDD);
        RouletteEventDbDto currentEvent = rouletteMapper.selectCurrentEvent(todayYmd);
        return currentEvent != null ? currentEvent : rouletteMapper.selectLatestEvent();
    }

    private RouletteDrawResDto buildDrawFailure(String errMsg) {
        return RouletteDrawResDto.builder()
                .resultCode(200)
                .resultMsg("정상 처리되었습니다.")
                .resultData(RouletteDrawResDto.ResultData.builder()
                        .isSuccess(false)
                        .errMsg(errMsg)
                        .ecEventDraw(List.of(
                                RouletteDrawResDto.EcEventDraw.builder()
                                        .isWinning("0")
                                        .winningCouponName(null)
                                        .build()
                        ))
                        .build())
                .build();
    }

    private RoulettePrizeDbDto pickPrizeByWeight(List<RoulettePrizeDbDto> prizeRows) {
        int totalWeight = prizeRows.stream()
                .map(RoulettePrizeDbDto::getWinningRate)
                .filter(Objects::nonNull)
                .mapToInt(rate -> Math.max(rate, 0))
                .sum();

        if (totalWeight <= 0) {
            return prizeRows.get(random.nextInt(prizeRows.size()));
        }

        int point = random.nextInt(totalWeight) + 1;
        int cumulative = 0;
        for (RoulettePrizeDbDto row : prizeRows) {
            cumulative += Math.max(row.getWinningRate() != null ? row.getWinningRate() : 0, 0);
            if (point <= cumulative) {
                return row;
            }
        }

        return prizeRows.get(prizeRows.size() - 1);
    }

    private void ensureDefaultTargetAndBalance(Long eventId, String acntNo) {
        if (eventId == null || acntNo == null || acntNo.isBlank()) {
            return;
        }
        rouletteMapper.upsertDefaultTarget(eventId, acntNo);
        rouletteMapper.upsertDefaultRaffleBalance(eventId, acntNo);
    }
}

