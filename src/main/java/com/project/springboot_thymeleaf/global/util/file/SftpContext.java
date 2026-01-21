package com.project.springboot_thymeleaf.global.util.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SftpContext implements RemoteFileStorageContext {

    private final DefaultSftpSessionFactory sftpSessionFactory;

    @Override
    public void upload(File file, String remotePath) {
        log.info("SFTP 업로드 시작: {} -> {}", file.getName(), remotePath);

        // Java 21의 가상 스레드가 처리하기 좋은 I/O 구조
        try (SftpSession session = sftpSessionFactory.getSession();
             FileInputStream fis = new FileInputStream(file)) {

            session.write(fis, remotePath + "/" + file.getName());
            log.info("SFTP 업로드 성공: {}", file.getName());

        } catch (Exception e) {
            log.error("SFTP 전송 중 에러 발생: {}", e.getMessage());
            throw new RuntimeException("SFTP 전송 실패", e);
        }
    }
}