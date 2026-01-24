package com.project.springboot_thymeleaf.global.sftp.service.impl;

import com.project.springboot_thymeleaf.global.sftp.config.SftpConfig;
import com.project.springboot_thymeleaf.global.sftp.service.SftpContext;
import com.project.springboot_thymeleaf.global.sftp.service.SftpService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.jcraft.jsch.ChannelSftp;

import java.util.List;

@Slf4j
@Service
public class SftpServiceImpl extends SftpService {

    @Resource
    private SftpConfig sftpConfig;

    /**
     * 멀티 호스트 파일 업로드 (yml의 directory 경로 활용)
     */
    public void uploadFiles(List<String> hosts, MultipartFile mFile) {
        hosts.forEach(host -> {
            try (SftpContext context = new SftpContext(host, sftpConfig)) {
                ChannelSftp sftp = context.getChannelSftp();

                // yml에서 가져온 기본 디렉토리 설정 활용
                String remoteDir = sftpConfig.getDirectory();
                String fileName = mFile.getOriginalFilename();

                sftp.put(mFile.getInputStream(), remoteDir + fileName);
                log.info("[성공] 서버: {}, 경로: {}", host, remoteDir + fileName);

            } catch (Exception e) {
                log.error("[실패] 서버: {}, 사유: {}", host, e.getMessage());
            }
        });
    }

    /**
     * 멀티 호스트 파일 삭제
     */
    public void deleteFiles(List<String> hosts, String fileName) {
        hosts.forEach(host -> {
            try (SftpContext context = new SftpContext(host, sftpConfig)) {
                String targetPath = sftpConfig.getDirectory() + fileName;
                context.getChannelSftp().rm(targetPath);
                log.info("[삭제완료] 서버: {}, 대상: {}", host, targetPath);
            } catch (Exception e) {
                log.error("[삭제실패] 서버: {}, 에러: {}", host, e.getMessage());
            }
        });
    }
}
