package com.project.springboot_thymeleaf.global.util.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
public class SftpConfig {

    @Value("${sftp.host}") private String host;
    @Value("${sftp.port}") private int port;
    @Value("${sftp.user}") private String user;
    @Value("${sftp.password}") private String password;

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(user);
        factory.setPassword(password);
        factory.setAllowUnknownKeys(true); // SSH 지문 체크 건너뛰기
        return factory;
    }
}