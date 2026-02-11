package com.project.springboot_thymeleaf.global.sftp.service.impl;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.project.springboot_thymeleaf.global.sftp.config.SftpConfig;
import com.project.springboot_thymeleaf.global.sftp.service.SftpContext;
import com.project.springboot_thymeleaf.global.sftp.service.SftpService;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private void executeSftp(String subDir, SftpTask task) throws Exception {
        for (String host : sftpConfig.getHosts()) {
            try (SftpContext context = new SftpContext(host, sftpConfig)) {
                // subDir : 서버 루트 경로 밑으로 서브 디렉토리 경로
                task.doWork(context.getChannelSftp(), subDir);
                log.info("[작업성공] 서버: {}, 경로: {}", host, subDir);
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
    public void uploadToRemote(MultipartFile mFile, String subDir, String customFileNm) throws Exception {
        // 파일 유효성 체크 (비어있는지 확인)
        if (mFile == null || mFile.isEmpty()) {
            log.warn("[전송중단] 업로드할 파일이 비어있습니다.");
            return;
        }

        final String contentType = mFile.getContentType();
        final boolean isImg = !StringUtils.isEmpty(contentType) && contentType.startsWith("image");
        // 이미지일 경우 fileData 및 fileNm 변환
        final byte[] finalFileData = getFinalData(isImg, mFile);
        final String finalFileNm = getFinalNm(isImg, customFileNm);

        // 엑셀 업로드 업무 수행
        executeSftp(subDir, (channelSftp, targetPath) -> {
            try (InputStream is = new ByteArrayInputStream(finalFileData)) {
                createDir(channelSftp, targetPath);
                channelSftp.put(is, finalFileNm);
            }
        });

    }

    /**
     * 파일 삭제 공통 함수
     * @param subDir : 서브(하위) 디렉토리
     * @param customFileNm : 커스텀 파일명
     */
    @Override
    public void deleteFromRemote(String subDir, String customFileNm) throws Exception{
        executeSftp(subDir, (channelSftp, targetPath) ->
                channelSftp.rm(targetPath + "/" + customFileNm));
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
                            channelSftp.chmod(0775, nextPath);
                            channelSftp.cd(nextPath);
                        } catch (SftpException ex) {
                            log.error("SFTP 폴더 이미 존재 : {}", nextPath);
                        }

                    }
                    return nextPath;
        });
    }

    private byte[] getFinalData(boolean isImg, MultipartFile mFile) throws  Exception{
        // 이미지일 경우 최적화
        if(isImg) {
            return optimizeImage(mFile);
        }

        return mFile.getBytes();
    }

    private String getFinalNm(boolean isImg, String fileNm){
        // 이미지일 경우 확장자 변경
        if(isImg){
            return fileNm.replaceAll("\\.[^.]+$" , ".jpg");
        }

        return fileNm;
    }

    /**
     * 고해상도 이미지를 서버 저장용으로 최적화 메소드
     * @param mFile 업로드된 원본 파일
     * @return 최적화된 바이트 배열
     */
    public static byte[] optimizeImage(MultipartFile mFile) throws Exception {
        // 1. 비어있는 파일 체크
        if (mFile == null || mFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 2. 캐시 생성 방지
        ImageIO.setUseCache(false);
        // 3. 최적화 처리
        try (InputStream is = mFile.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            Thumbnails.of(is)
                    .size(1920, 1080)  // FHD 해상도로 리사이징 (비율 유지)
                    .outputQuality(0.8)   // 화질 80% (용량 절감의 핵심)
                    .imageType(BufferedImage.TYPE_INT_RGB) // 불필요한 투명 채널 제거 (용량 추가 절감)
                    .outputFormat("jpg")  // 메타데이터 제거 및 JPG로 변환
                    .toOutputStream(os);

            return os.toByteArray();
        }
    }

    @Override
    public void downloadToLocal(String subDir, String fileNm) throws Exception {

        // 다운로드 변수값 설정
        SftpConfig.Download info = sftpConfig.getDownload();
        SftpConfig downConfig = new SftpConfig();
        downConfig.setUser(info.getUser());
        downConfig.setPassword(info.getPassword());
        downConfig.setPort(info.getPort());

        try (SftpContext context = new SftpContext(info.getHost(), downConfig)) {

            ChannelSftp channel = context.getChannelSftp();
            String remoteFile = subDir + "/" + fileNm;
            Path localPath = Paths.get(info.getLocalRootPath());

            // 폴더 없을 경우 부모 폴더까지 생성
            if (!Files.exists(localPath)) {
                Files.createDirectories(localPath);
            }

            Path targetFile = localPath.resolve(fileNm);

            // 3. 파일 땡겨오기
            try (InputStream is = channel.get(remoteFile)) {
                Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);
                log.info("[다운로드성공] 로컬저장경로: {}", targetFile);
            }
        }
    }
}
