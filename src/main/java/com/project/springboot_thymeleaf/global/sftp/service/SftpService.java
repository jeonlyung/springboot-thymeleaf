package com.project.springboot_thymeleaf.global.sftp.service;

import org.springframework.web.multipart.MultipartFile;

public interface SftpService {
    public void uploadToRemote(MultipartFile mFile, String subDir, String customFileNm) throws Exception;

    public void deleteFromRemote(String subDir, String customFileNm) throws Exception;

    public void downloadToLocal(String subDir, String fileNm) throws Exception;
}
