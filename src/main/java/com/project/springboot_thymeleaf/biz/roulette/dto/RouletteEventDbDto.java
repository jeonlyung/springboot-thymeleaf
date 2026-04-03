package com.project.springboot_thymeleaf.biz.roulette.dto;

import lombok.*;

/**
 * 룰렛 이벤트 DB 조회용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteEventDbDto {

    private Long eventId;
    private String eventName;
    private String eventStartDate;
    private String eventEndDate;
    private String dispContents;
}

