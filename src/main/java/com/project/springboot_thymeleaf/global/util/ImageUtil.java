package com.project.springboot_thymeleaf.global.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class ImageUtil {

    /**
     * 고해상도 이미지를 서버 저장용으로 최적화 메소드
     * @param mFile 업로드된 원본 파일
     * @return 최적화된 바이트 배열
     */
    public byte[] compressImgResize(MultipartFile mFile) throws Exception {
        // 1. 비어있는 파일 체크
        if (mFile == null || mFile.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 2. 최적화 처리
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
}
