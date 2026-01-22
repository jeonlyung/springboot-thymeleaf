package com.project.springboot_thymeleaf.global.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // src/main/resources/messages.properties 파일을 읽음
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setDefaultLocale(Locale.KOREA);
        // 코드가 없을 때 에러 내지 않고 코드명 그대로 노출
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }
}