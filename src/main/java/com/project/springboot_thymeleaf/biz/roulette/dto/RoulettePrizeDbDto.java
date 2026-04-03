package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

/**
 * 룰렛 경품 DB 조회용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoulettePrizeDbDto {

    private Long prizeId;
    private Long eventId;
    private String couponName;
    private String couponCode;
    private Integer winningRate;
    private String winningPrizeYn;
}

