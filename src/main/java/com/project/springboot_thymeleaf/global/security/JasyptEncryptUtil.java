package com.project.springboot_thymeleaf.global.security;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * Jasypt 암호화 유틸리티
 */
public class JasyptEncryptUtil {

    public static void main(String[] args) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("my_secret_key"); // 실제 운영 환경에서 사용할 키
        encryptor.setAlgorithm("PBEWithMD5AndDES");

        // 암호화할 값들
        String[] secrets = {

        };

        String[] labels = {

        };

        for (int i = 0; i < secrets.length; i++) {
            String encrypted = encryptor.encrypt(secrets[i]);
            System.out.println(labels[i] + ":");
            System.out.println("  원본: " + secrets[i]);
            System.out.println("  암호화: ENC(" + encrypted + ")");
            System.out.println();
        }


    }
}

