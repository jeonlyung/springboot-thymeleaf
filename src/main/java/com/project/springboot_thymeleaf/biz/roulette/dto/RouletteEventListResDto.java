package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

import java.util.List;

/**
 * IF-CX-047 이벤트 정보 조회 응답 DTO
 * POST /api/v1/evm/ec-event-list
 * Response: resultCode, resultMsg,
 *   resultData: { isSuccess, errMsg,
 *     ecEventList: [{ eventId, eventName, eventStartDate, eventEndDate, ... }] }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteEventListResDto {

    /** 결과 코드 (Number) */
    private Integer resultCode;

    /** 결과 메시지 (String) */
    private String resultMsg;

    /** 결과 데이터 */
    private ResultData resultData;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultData {

        /** 성공 여부 */
        private Boolean isSuccess;

        /** 에러 메시지 */
        private String errMsg;

        /** 이벤트 목록 */
        private List<EcEvent> ecEventList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EcEvent {

        /** 이벤트 아이디 */
        private Long eventId;

        /** 이벤트 명 */
        private String eventName;

        /** 이벤트 시작일 (yyyyMMdd) */
        private String eventStartDate;

        /** 이벤트 종료일 (yyyyMMdd) */
        private String eventEndDate;
    }
}

