package com.project.springboot_thymeleaf.biz.login.mapper;

import com.project.springboot_thymeleaf.biz.login.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;
import java.util.Optional;

@Mapper
public interface LoginMapper {
    // 유저 아이디로 회원 정보 조회
    Optional<MemberDto> findByUsrId(MemberDto memberDto);

    // OAuth2 제공자와 제공자 ID로 회원 정보 조회
    MemberDto findByProviderAndProviderId(Map<String, String> params);

    // 신규 회원 가입
    void saveUsrInfo(MemberDto memberDto);

    // OAuth2 사용자 정보 업데이트 (로그인 시간, 프로필 이미지 등)
    void updateOAuthUserInfo(MemberDto memberDto);
}
