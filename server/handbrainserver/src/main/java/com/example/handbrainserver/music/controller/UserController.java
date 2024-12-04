package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.service.FriendService;
import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.JwtUtil;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    @Autowired
    private JwtUtil jwtUtil;
    private final UserService userService;
    private final FriendService friendService;
    @Autowired
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
            Long userId = Long.parseLong(jwtUtil.extractUserId(processedToken));
            
            if(userService.isUserExistsById(userId)){
                return ResponseEntity.ok(Map.of("state","success"));
            }else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("state","failed")
                );
            }
        }catch(Exception e){
            e.printStackTrace();
            response.put("state","token expired");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/get/myname")
    public ResponseEntity<?> getMyName(
            @RequestHeader("Authorization") String token
    ){
        String processedToken = token.replace("Bearer ", ""); // Bearer 제거
        Long userId = Long.parseLong(jwtUtil.extractUserId(processedToken));
        String userName = userService.getUserById(userId).getName();
        return ResponseEntity.ok(Map.of("name",userName));
    }

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
        Long userId = Long.parseLong(jwtUtil.extractUserId(processedToken));
        try {
            Integer friendNum = friendService.updateFriends(userId, contacts);
            return ResponseEntity.ok(Map.of(
                "status","success",
                "friendNum", friendNum
            ));
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "서버 오류 발생"));
        }
    }

}
