package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * IF-CX-051 당첨자 선정 응답 DTO
 * Response: resultCode, resultMsg, resultData(isSuccess, errMsg, ecEventDraw[isWinning, winningCouponName])
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteDrawResDto {

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

        /** 성공 여부 (Boolean, true/false) */
        private Boolean isSuccess;

        /** 에러 메시지 (String, 길이 200) */
        private String errMsg;

        /** 이벤트 추첨 정보 목록 (Object Array) */
        private List<EcEventDraw> ecEventDraw;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EcEventDraw {

        /**
         * 당첨 여부 (String)
         * 꽝이 있는 경우 0
         */
        private String isWinning;

        /** 당첨 쿠폰명 (Number) */
        private String winningCouponName;
    }
}

