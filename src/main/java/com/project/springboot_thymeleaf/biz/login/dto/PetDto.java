package com.project.springboot_thymeleaf.biz.login.dto;

import lombok.Builder;

@Builder
public record PetDto(
        Long petSeq,
        String usrId,
        String petNm,
        String dogBreed,
        String petBirth,
        String petImg
) {
}
