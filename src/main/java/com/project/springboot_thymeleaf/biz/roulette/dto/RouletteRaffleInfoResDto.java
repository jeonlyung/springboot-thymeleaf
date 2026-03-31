package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * IF-CX-050 응모권 횟수 조회 응답 DTO
 * Response: resultCode, resultMsg, resultData(isSuccess, errMsg, ecEventRaffleInfo[raffleTicketCount])
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteRaffleInfoResDto {

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

        /** 이벤트 응모 정보 목록 (Object Array) */
        private List<EcEventRaffleInfo> ecEventRaffleInfo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EcEventRaffleInfo {

        /** 응모권 개수 (Number) */
        private Integer raffleTicketCount;
    }
}

