package com.project.springboot_thymeleaf.global.security;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.spring31.properties.EncryptablePropertySourcesPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class JasyptConfig {

    // 1. 실제 복호화를 담당하는 엔진 Bean
    @Bean("jasyptStringEncryptor")
    public static StandardPBEStringEncryptor jasyptStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        // 실행 시 -Djasypt.password=... 로 주입
        encryptor.setPassword(System.getProperty("jasypt.password", "default_key"));
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        return encryptor;
    }

    // 2. ${} 안에 ENC()가 있으면 자동으로 복호화해서 교체해주는 Bean (핵심)
    @Bean
    public static EncryptablePropertySourcesPlaceholderConfigurer propertyConfigurer() {
        // org.jasypt.spring31... 패키지에서 제공하는 클래스입니다.
        return new EncryptablePropertySourcesPlaceholderConfigurer(jasyptStringEncryptor());
    }
}