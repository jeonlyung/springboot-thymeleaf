package com.project.springboot_thymeleaf.biz.login.service.impl;

import com.project.springboot_thymeleaf.biz.login.dto.CustomOAuth2User;
import com.project.springboot_thymeleaf.biz.login.dto.MemberDto;
import com.project.springboot_thymeleaf.biz.login.dto.OAuthAttributes;
import com.project.springboot_thymeleaf.biz.login.mapper.LoginMapper;
import com.project.springboot_thymeleaf.biz.login.service.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthServiceImpl implements OauthService {

    private final LoginMapper loginMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용하여 사용자 정보 가져오기
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // OAuth2 제공자 정보 (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // OAuth2 로그인 시 키가 되는 필드값 (PK)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        // OAuth2 제공자별 사용자 정보 추출
        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        log.info("OAuth2 로그인 - Provider: {}, User: {}", registrationId, attributes.getName());

        // 사용자 정보를 MemberDto로 변환
        MemberDto member = attributes.toEntity();

        // DB에 사용자 정보 저장 또는 업데이트
        saveOrUpdate(member);

        // CustomOAuth2User 반환 (Spring Security가 세션에 저장)
        return new CustomOAuth2User(member, attributes.getAttributes(), attributes.getNameAttributeKey());
    }

    /**
     * OAuth2 사용자 정보 저장 또는 업데이트
     * - 신규 사용자: INSERT
     * - 기존 사용자: UPDATE (프로필 이미지, 이름 등)
     */
    private void saveOrUpdate(MemberDto member) {
        // Provider와 ProviderId로 기존 사용자 조회
        Map<String, String> params = new HashMap<>();
        params.put("provider", member.provider());
        params.put("providerId", member.providerId());

        MemberDto existingUser = loginMapper.findByProviderAndProviderId(params);

        if (existingUser != null) {
            // 기존 사용자 - 정보 업데이트 (프로필 이미지, 이름 등)
            log.info("기존 사용자 로그인: {} ({})", member.usrId(), member.provider());
            loginMapper.updateOAuthUserInfo(member);
        } else {
            // 신규 사용자 - 회원가입
            log.info("신규 사용자 가입: {} ({})", member.usrId(), member.provider());
            loginMapper.saveUsrInfo(member);
        }
    }
}
