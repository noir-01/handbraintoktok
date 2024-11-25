package com.example.handbrainserver.music.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Random;

@Service
public class SmsService {

    @Value("${sms.api.clientId}")
    private String smsId; // SMS ID
    @Value("${sms.api.apiKey}")
    private String apiKey; // API Key
    @Value("${sms.callBack}")
    private String callBackPhoneNumber; //발신번호

    private final RestTemplate restTemplate;
    private final RedisService redisService;

    public SmsService(RestTemplate restTemplate, RedisService redisService) {
        this.restTemplate = restTemplate;
        this.redisService = redisService;
    }

    // SMS 토큰 발급
    public String getToken() {
        String encodedString = encodeToBase64(smsId, apiKey);

        String url = "https://sms.gabia.com/oauth/token";
        String payload = "grant_type=client_credentials";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedString);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // 토큰 추출
        String token = parseToken(response.getBody());
        return token;
    }

    // 6자리 랜덤 인증번호 생성
    public String generateRandomCode() {
        Random random = new Random();
        int code = random.nextInt(999999 - 100000) + 100000; // 6자리 수
        return String.valueOf(code);
    }

    // 인증번호를 Redis에 저장
    public void storeCodeInRedis(String phoneNumber, String code) {
        // Redis에 30분(1800초) 동안 저장
        redisService.setWithExpiration(phoneNumber, code, 1800);
    }

    // SMS 전송
    public void sendSms(String phoneNumber, String code) {
        String token = getToken();  // 토큰 발급

        // 인증번호와 전화번호를 사용해 SMS 발송
        String encodedString = encodeToBase64(smsId, token);

        String url = "https://sms.gabia.com/api/send/sms";
        String payload = "phone=" + phoneNumber + "&callback=" + callBackPhoneNumber 
            + "&message=" + code + "&refkey=[[RESTAPITEST1549847130]]";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedString);

        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
    }

    // Base64 인코딩
    private String encodeToBase64(String smsId, String apiKey) {
        String auth = smsId + ":" + apiKey;
        return Base64.getEncoder().encodeToString(auth.getBytes());
    }

    private String parseToken(String responseBody) {
        try {
            // Jackson의 ObjectMapper로 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // 'access_token' 값을 추출
            return rootNode.path("access_token").asText();  // 'access_token'을 문자열로 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // 예외 처리: 문제가 있을 경우 null 반환
        }
    }

    public boolean verifyCode(String phoneNumber, String enteredCode) {
        String storedCode = redisService.get(phoneNumber);
        return storedCode != null && storedCode.equals(enteredCode);
    }
}

