package com.project.springboot_thymeleaf.global.sftp.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.project.springboot_thymeleaf.global.sftp.config.SftpConfig;
import com.project.springboot_thymeleaf.global.sftp.service.SftpContext;
import com.project.springboot_thymeleaf.global.sftp.service.SftpService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@Service
public class SftpServiceImpl implements SftpService {

    @Resource
    private SftpConfig sftpConfig;

    /**
     * 파일 업로드 공통 함수
     * @param mFile
     * @param subDir
     * @param customFileNm
     */
    @Override
    public void uploadFile(MultipartFile mFile, String subDir, String customFileNm) {
        // yml에서 읽어온 hosts 리스트를 순회합니다.
        for (String host : sftpConfig.getHosts()) {
            try (SftpContext context = new SftpContext(host, sftpConfig);
                 InputStream is = mFile.getInputStream()) {
                ChannelSftp channelSftp = context.getChannelSftp();
                String targetPath = sftpConfig.getDirectory() + "/" + subDir + customFileNm;

                //폴더 생성
                createDir(channelSftp, targetPath);
                //파일 전송
                channelSftp.put(is, targetPath);
                log.info("[전송성공] 서버: {} ", host);
            } catch (Exception e) {
                // 한 서버에서 실패해도 멈추지 않고 다음 서버로 넘어갑니다.
                log.error("[전송실패] 서버: {}, 원인: {}", host, e.getMessage());
            }
        }
    }

    /**
     * 파일 삭제 공통 함수
     * @param subDir
     * @param customFileNm
     */
    @Override
    public void deleteFile(String subDir, String customFileNm) {
        for (String host : sftpConfig.getHosts()) {
            try (SftpContext context = new SftpContext(host, sftpConfig)) {
                String targetPath = sftpConfig.getDirectory() + "/" + subDir + customFileNm;
                context.getChannelSftp().rm(targetPath);
                log.info("[삭제성공] 서버: {}", host);
            } catch (Exception e) {
                log.error("[삭제실패] 서버: {}, 원인: {}", host, e.getMessage());
            }
        }
    }

    private void createDir(ChannelSftp channelSftp, String targetPath) throws Exception {
        // Stream reduce를 활용해 단계별 폴더 존재 확인 및 생성
        Arrays.stream(targetPath.split("/"))
                .filter(f -> !f.isEmpty())
                .reduce("", (base, folder) -> {
                    String nextPath = base + "/" + folder;
                    try {
                        channelSftp.cd(nextPath);
                    } catch (SftpException e) {
                        try {
                            channelSftp.mkdir(nextPath);
                            channelSftp.cd(nextPath);
                        } catch (SftpException ex) {
                            log.error("SFTP 폴더 이미 존재 : {}", nextPath);
                        }

                    }
                    return nextPath;
                });
    }
}
