package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

import java.util.List;

/**
 * IF-CX-049 이벤트 혜택 구성 정보 조회 응답 DTO
 * POST /api/v1/evm/ec-event-prize-list
 * Response: resultCode, resultMsg,
 *   resultData: { isSuccess, errMsg,
 *     ecEventPrizeList: [{ couponName, ... }] }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteEventPrizeListResDto {

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

        /** 혜택(경품) 목록 */
        private List<EcEventPrize> ecEventPrizeList;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EcEventPrize {

        /** 쿠폰명 */
        private String couponName;

        /** 쿠폰 코드 */
        private String couponCode;

        /** 당첨 확률(%) */
        private Integer winningRate;
    }
}

