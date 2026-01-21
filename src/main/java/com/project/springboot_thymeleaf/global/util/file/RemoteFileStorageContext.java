package com.project.springboot_thymeleaf.global.util.file;

import java.io.File;

public interface RemoteFileStorageContext {
    /**
     * 원격 서버로 파일 업로드
     * @param file 전송할 로컬 파일 객체
     * @param remotePath 원격 저장소 경로
     */
    void upload(File file, String remotePath);
}