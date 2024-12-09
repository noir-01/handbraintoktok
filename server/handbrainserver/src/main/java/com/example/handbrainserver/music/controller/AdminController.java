package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.service.AdminService;
import com.example.handbrainserver.music.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // login.html로 이동
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, @RequestParam String password, Model model) {
        // 여기서 adminService에서 비밀번호를 검증하는 로직이 들어가야 합니다.
        boolean isAuthenticated = adminService.adminLogin(userId, password);

        if (isAuthenticated) {
            // 비밀번호가 맞으면 JWT 토큰 생성
            String token = jwtUtil.generateToken(adminService.getAdminIdLong(userId));
            // 토큰을 모델에 담아서 /admin/upload로 전달
            model.addAttribute("token", token);
            return "redirect:/admin/upload"; // 로그인 성공 후 /admin/upload로 리다이렉션
        } else {
            // 인증 실패 시 로그인 페이지로 다시 리다이렉션
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
}
