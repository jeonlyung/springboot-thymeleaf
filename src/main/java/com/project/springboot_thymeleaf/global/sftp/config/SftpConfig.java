package com.project.springboot_thymeleaf.global.sftp.config;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpConfig {

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("192.168.0.10");
        factory.setPort(22);
        factory.setUser("webuser");
        factory.setPassword("your_password"); // 실무에선 Jasypt 사용
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory); // 연결 재사용을 위한 캐싱
    }

    @Bean
    public SftpRemoteFileTemplate sftpTemplate(SessionFactory<SftpClient.DirEntry> sf) {
        return new SftpRemoteFileTemplate(sf);
    }
}