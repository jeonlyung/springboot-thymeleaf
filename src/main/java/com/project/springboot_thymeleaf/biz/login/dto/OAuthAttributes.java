package com.project.springboot_thymeleaf.biz.login.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 정보를 추출하는 DTO
 */
@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String provider;
    private String providerId;

    /**
     * OAuth2 제공자에 따라 사용자 정보 추출
     */
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        // 제공자별 분기 처리
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        // Google (기본)
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * Google 사용자 정보 추출
     */
    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                           Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider("google")
                .providerId((String) attributes.get("sub"))
                .build();
    }

    /**
     * Kakao 사용자 정보 추출
     * (account_email 권한이 없을 경우 대비)
     */
    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                          Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        // providerId를 기반으로 고유한 사용자 ID 생성 (이메일 권한이 없는 경우 대비)
        String providerId = String.valueOf(attributes.get("id"));
        String usrId = "kakao_" + providerId;  // 예: kakao_1234567890

        // 카카오 프로필 이미지 URL 처리 (고해상도로 변경)
        String profileImageUrl = (String) profile.get("profile_image_url");
        if (profileImageUrl != null && profileImageUrl.contains("thumbnail")) {
            // thumbnail을 original로 변경하여 고해상도 이미지 사용
            profileImageUrl = profileImageUrl.replace("/C110x110", "/C640x640")
                                             .replace("thumbnail", "original");
        }

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email(usrId)  // 이메일 대신 kakao_providerId 형식 사용
                .picture(profileImageUrl)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .provider("kakao")
                .providerId(providerId)
                .build();
    }

    /**
     * Naver 사용자 정보 추출
     */
    private static OAuthAttributes ofNaver(String userNameAttributeName,
                                          Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .provider("naver")
                .providerId((String) response.get("id"))
                .build();
    }

    /**
     * MemberDto로 변환
     */
    public MemberDto toEntity() {
        return MemberDto.builder()
                .usrId(email)
                .usrNm(name)
                .profileImg(picture)
                .provider(provider)
                .providerId(providerId)
                .useYn("Y")
                .build();
    }
}

