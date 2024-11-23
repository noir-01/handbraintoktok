package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.JwtUtil;
import io.jsonwebtoken.Jwt;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private JwtUtil jwtUtil = new JwtUtil();
    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login"; // 로그인 페이지 템플릿 이름
    }
    @PostMapping(value="/login",consumes = "application/x-www-form-urlencoded")
    public void login(@RequestParam String username, @RequestParam String token) {
        if(jwtUtil.validateToken(token,username)) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(token, null, new ArrayList<>());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
    @Getter
    private class LoginRequest{
        private String phoneNumber;
        private String token;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String name) {
        JwtUtil jwtUtil = new JwtUtil();
        Long userId = userService.saveUser(name);
        if (userId!=null) {
            Map<String, String> response = new HashMap<>();
            response.put("token", jwtUtil.generateToken(userId));
            return ResponseEntity.ok(response); // 성공적인 경우 JSON 응답 반환
        } else {
            return ResponseEntity.badRequest().body("failed"); // 실패 시 JSON 응답 반환
        }
    }
    @GetMapping("/home")
    public String home(Model model) {
        // 로그인 후 필요한 사용자 정보를 모델에 추가
        return "home"; // home.html 템플릿을 반환
    }
}
