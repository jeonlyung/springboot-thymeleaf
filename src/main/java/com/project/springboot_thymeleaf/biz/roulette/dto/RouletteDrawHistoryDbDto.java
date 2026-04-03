package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

/**
 * 룰렛 추첨 이력 저장용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteDrawHistoryDbDto {

    private Long eventId;
    private String acntNo;
    private String drawResultCode;
    private String winningCouponName;
    private String winningCouponCode;
    private String regId;
}

