package com.example.handbrainserver.music.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoUtil {
    private static final String AES = "AES";
    private static final String SECRET_KEY = "asdfasdfasdfasdf"; // 환경 변수로 관리 추천

    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decodedBytes));
    }
}

