package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.service.SmsService;
import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.JwtUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/sms")
@RestController
public class SmsController {

    private final SmsService smsService;
    private final UserService userService;
    @Autowired
    public SmsController(SmsService smsService, UserService userService) {
        this.smsService = smsService;
        this.userService = userService;
    }
    @Getter @Setter
    private static class NumDto{
        private String phoneNumber;
    }
    @PostMapping("/send")
    public String sendSms(@RequestBody NumDto numDto) {
        // 랜덤 인증번호 생성
        String code = smsService.generateRandomCode();
        // Redis에 저장
        smsService.storeCodeInRedis(numDto.getPhoneNumber(), code);
        // SMS 발송
        smsService.sendSms(numDto.getPhoneNumber(), code);
        return "SMS sent successfully!";
    }
    @Getter @Setter
    private static class VerificationRequest {
        private String phoneNumber;
        private String code;
        private String name;
    }

    //인증번호 검증, 검증 성공 시 회원가입까지.
    @PostMapping("/verify/register")
    public ResponseEntity<?> verifyCode(@RequestBody VerificationRequest verificationRequest) {
        String phoneNumber = verificationRequest.getPhoneNumber();
        String code = verificationRequest.getCode();
        String name = verificationRequest.getName();
        if (smsService.verifyCode(phoneNumber, code)) {
            JwtUtil jwtUtil = new JwtUtil();
            Long userId = userService.saveUser(new UserDto.UserDtoWithOutId(name, phoneNumber));
            System.out.println("userid: "+userId.toString());
            if (userId != -1) {
                Map<String, String> response = new HashMap<>();
                response.put("token", jwtUtil.generateToken(userId));
                return ResponseEntity.ok(response); // 성공적인 경우 JSON 응답 반환
            }
        }
        return ResponseEntity.badRequest().body("failed"); // 실패 시 JSON 응답 반환
    }
}
