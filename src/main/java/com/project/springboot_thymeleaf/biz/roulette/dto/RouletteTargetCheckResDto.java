package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * IF-CX-052 이벤트 신청대상 확인 응답 DTO
 * Response: resultCode, resultMsg, resultData(isSuccess, errMsg, ecEventTargetCheck[isTarget, targetCouponName])
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteTargetCheckResDto {

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

        /** 이벤트 대상 확인 목록 (Object Array) */
        private List<EcEventTargetCheck> ecEventTargetCheck;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EcEventTargetCheck {

        /**
         * 대상여부 (String)
         * 0: 미대상, 1: 대상
         */
        private String isTarget;

        /**
         * 대상 쿠폰명 (Number)
         * 내정된 경우 설정된 쿠폰 발행
         */
        private String targetCouponName;
    }
}

