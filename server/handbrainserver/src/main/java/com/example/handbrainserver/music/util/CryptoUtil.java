package com.example.handbrainserver.music.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import jakarta.annotation.PostConstruct;

@Component
public class CryptoUtil {
    private static final String AES = "AES";
    private static String SECRET_KEY;
    @Value("${secret_key}")
    private String secret_key;

    @PostConstruct
    public void init() {
        this.SECRET_KEY = secret_key;
    }

    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        return new String(cipher.doFinal(decodedBytes));
    }
}

