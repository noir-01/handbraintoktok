package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.service.FriendService;
import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.JwtUtil;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private JwtUtil jwtUtil = new JwtUtil();
    private final UserService userService;
    private final FriendService friendService;
    public UserController(UserService userService, FriendService friendService){

        this.userService = userService;
        this.friendService = friendService;
    }

    @PostMapping("/login/token")
    public ResponseEntity<?> login(
        @RequestHeader("Authorization") String token
    ){
        Map<String, String> response = new HashMap<>();

        try {
            String processedToken = token.replace("Bearer ", ""); // Bearer 제거
            JwtUtil jwtUtil = new JwtUtil();
            Long userId = Long.parseLong(jwtUtil.extractUserId(processedToken));
            response.put("state","success");
            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            response.put("state","token expired");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @Getter
    private class LoginRequest{
        private String phoneNumber;
        private String token;
    }

//    @PostMapping("/register")
//    public ResponseEntity<?> register(
//            @RequestBody UserDto.UserDtoWithOutId userDtoWithOutId
//    ) {
//        JwtUtil jwtUtil = new JwtUtil();
//        Long userId = userService.saveUser(
//                new UserDto.UserDtoWithOutId(userDtoWithOutId.getName(), userDtoWithOutId.getPhoneNumber())
//        );
//
//        if(userId!=-1){
//            Map<String, String> response = new HashMap<>();
//            response.put("token", jwtUtil.generateToken(userId));
//            return ResponseEntity.ok(response); // 성공적인 경우 JSON 응답 반환
//        }else{
//            return ResponseEntity.badRequest().body("failed"); // 실패 시 JSON 응답 반환
//        }
//    }

    @GetMapping("/home")
    public String home(Model model) {
        // 로그인 후 필요한 사용자 정보를 모델에 추가
        return "home"; // home.html 템플릿을 반환
    }

    @PostMapping("/friend/upload")
    public ResponseEntity<?> friendRelation(
            @RequestHeader("Authorization") String token,
            @RequestBody List<String> contacts
    ){
        String processedToken = token.replace("Bearer ", ""); // Bearer 제거
        JwtUtil jwtUtil = new JwtUtil();
        Long userId = Long.parseLong(jwtUtil.extractUserId(processedToken));
        try {
            friendService.updateFriends(userId, contacts);
            return ResponseEntity.ok("success");
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body("Database error");
        }
    }

}
