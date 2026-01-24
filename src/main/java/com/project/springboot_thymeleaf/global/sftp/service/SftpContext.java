package com.project.springboot_thymeleaf.global.sftp.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.project.springboot_thymeleaf.global.sftp.config.SftpConfig;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
@Getter
public class SftpContext implements AutoCloseable {
    private final Session session;
    private final ChannelSftp channelSftp;

    @Resource(name = "sftpConfig")
    private SftpConfig sftpConfig;

    // 특정 호스트(멀티 대응)와 설정 객체를 받아 연결 (생성자)
    public SftpContext(String host, SftpConfig config) throws Exception {
        JSch jsch = new JSch();

        // 1. 세션 설정 (config에서 user, port 수령)
        this.session = jsch.getSession(config.getUser(), host, config.getPort());
        this.session.setPassword(config.getPassword());

        Properties props = new Properties();
        props.put("StrictHostKeyChecking", "no");
        props.put("PubkeyAcceptedAlgorithms", "plus-ssh-rsa"); // Java 21 보안 호환
        this.session.setConfig(props);

        this.session.connect(10000);

        // 2. 채널 할당 (사용자님 최적화 제안 반영)
        this.channelSftp = (ChannelSftp) this.session.openChannel("sftp");
        this.channelSftp.connect(10000);

        log.info("[SFTP] Successfully connected to {}", host);
    }

    @Override
    public void close() {
        if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
        if (session != null && session.isConnected()) session.disconnect();
    }
}