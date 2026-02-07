package com.project.springboot_thymeleaf.biz.login.mapper;

import com.project.springboot_thymeleaf.biz.login.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface LoginMapper {
    // 유저 아이디로 회원 정보 조회
    Optional<MemberDto> findByUsrId(MemberDto memberDto);

    // 신규 회원 가입
    void saveUsrInfo(MemberDto memberDto);
}
