package com.project.springboot_thymeleaf.biz.login.dto;

import lombok.Builder;

@Builder
public record MemberDto(
        String usrId,
        String usrPw,
        String usrNm,
        String profileImg,
        String provider,
        String providerId,
        String useYn
) {
}
