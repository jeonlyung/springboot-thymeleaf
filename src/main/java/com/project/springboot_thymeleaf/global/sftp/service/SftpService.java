package com.project.springboot_thymeleaf.global.sftp.service;

import org.springframework.web.multipart.MultipartFile;

public interface SftpService {
    public void uploadFile(MultipartFile mFile, String subDir, String customFileNm) throws Exception;

    public void deleteFile(String subDir, String customFileNm) throws Exception;
}
