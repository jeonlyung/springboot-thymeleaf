package com.project.springboot_thymeleaf.global.sftp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "sftp")
@Getter @Setter
public class SftpConfig {
    private List<String> hosts; // yml의 host
    private int port;           // yml의 port
    private String user;        // yml의 user
    private String password;    // yml의 password
    private String directory;   // yml의 directory
}

