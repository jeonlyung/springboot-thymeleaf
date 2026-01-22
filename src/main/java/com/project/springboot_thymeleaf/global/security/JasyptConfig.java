package com.project.springboot_thymeleaf.global.security;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JasyptConfig {

    @Bean("jasyptStringEncryptor")
    public StandardPBEStringEncryptor jasyptStringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        String password = System.getProperty("jasypt.password");

        encryptor.setPassword(password);
        encryptor.setAlgorithm("PBEWithMD5AndDES"); // 기본 알고리즘

        return encryptor;
    }
}