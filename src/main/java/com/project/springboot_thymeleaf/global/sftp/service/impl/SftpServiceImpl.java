package com.project.springboot_thymeleaf.global.sftp.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.project.springboot_thymeleaf.global.sftp.config.SftpConfig;
import com.project.springboot_thymeleaf.global.sftp.service.SftpContext;
import com.project.springboot_thymeleaf.global.sftp.service.SftpService;
import com.project.springboot_thymeleaf.global.util.ImageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@Service
public class SftpServiceImpl implements SftpService {

    @Resource
    private SftpConfig sftpConfig;

    // [공통화] 함수형 인터페이스
    @FunctionalInterface
    private interface SftpTask {
        void doWork(ChannelSftp channel, String path) throws Exception;
    }

    // [공통화] 서버 순회 및 자원 관리 전담
    private void executeSftp(String subDir, String customFileNm, SftpTask task) {
        String targetPath = sftpConfig.getDirectory() + "/" + subDir + customFileNm;

        for (String host : sftpConfig.getHosts()) {
            try (SftpContext context = new SftpContext(host, sftpConfig)) {
                task.doWork(context.getChannelSftp(), targetPath);
                log.info("[작업성공] 서버: {}, 경로: {}", host, targetPath);
            } catch (Exception e) {
                log.error("[작업실패] 서버: {}, 원인: {}", host, e.getMessage());
            }
        }
    }

    /**
     * 파일 업로드 공통 함수
     * @param mFile : 파일
     * @param subDir : 서브(하위) 디렉토리
     * @param customFileNm : 커스텀 파일명
     */
    @Override
    public void uploadFile(MultipartFile mFile, String subDir, String customFileNm) throws Exception {
        // 파일 유효성 체크 (비어있는지 확인)
        if (mFile == null || mFile.isEmpty()) {
            log.warn("[전송중단] 업로드할 파일이 비어있습니다.");
            return;
        }

        // 이미지 여부 확인 및 리사이징/최적화 수행
        byte[] uploadData;
        String contentType = mFile.getContentType();

        if (contentType != null && contentType.startsWith("image")) {
            // 이미지라면 리사이징 + 압축 + 메타데이터 제거 수행
            log.info("[이미지최적화 시작] 파일명: {}", mFile.getOriginalFilename());
            uploadData = ImageUtil.optimizeImage(mFile);
        } else {
            // 이미지가 아니라면 원본 데이터 그대로 사용
            uploadData = mFile.getBytes();
        }

        // 엑셀 업로드 업무 수행
        executeSftp(subDir, customFileNm, (channel, path) -> {
            try (InputStream is = new ByteArrayInputStream(uploadData)) {
                createDir(channel, path);
                channel.put(is, path);
            }
        });

    }

    /**
     * 파일 삭제 공통 함수
     * @param subDir : 서브(하위) 디렉토리
     * @param customFileNm : 커스텀 파일명
     */
    @Override
    public void deleteFile(String subDir, String customFileNm) throws Exception{
        executeSftp(subDir, customFileNm, ChannelSftp::rm);

    }

    private void createDir(ChannelSftp channelSftp, String targetPath) throws Exception {
        // Stream reduce를 활용해 단계별 폴더 존재 확인 및 생성
        Arrays.stream(targetPath.split("/"))
                .filter(f -> !f.isEmpty())
                .reduce("", (base, folder) -> {
                    String nextPath = base + "/" + folder;
                    try {
                        //폴더 있는지 체크
                        channelSftp.cd(nextPath);
                    } catch (SftpException e) {
                        //폴더 없으면 생성
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
