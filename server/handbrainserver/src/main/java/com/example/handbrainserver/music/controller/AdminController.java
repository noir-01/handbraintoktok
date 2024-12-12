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
import jakarta.servlet.http.HttpSession;

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
    public String login(HttpSession session, @RequestParam String userId, @RequestParam String password, Model model) {
        boolean isAuthenticated = adminService.adminLogin(userId, password);

        if (isAuthenticated) {
            // 비밀번호가 맞으면 JWT 토큰 생성
            String token = jwtUtil.generateToken(adminService.getAdminIdLong(userId));

            session.setAttribute("token", token);
            return "redirect:/admin/upload";
        } else {
            // 인증 실패 시 로그인 페이지로 다시 리다이렉션
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
    }
}
