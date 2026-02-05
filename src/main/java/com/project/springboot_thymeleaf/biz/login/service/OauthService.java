package com.project.springboot_thymeleaf.biz.login.service;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OauthService extends OAuth2UserService<OAuth2UserRequest, OAuth2User> {
}
