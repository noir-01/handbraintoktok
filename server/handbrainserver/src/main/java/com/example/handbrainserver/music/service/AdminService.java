package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.entity.Admin;
import com.example.handbrainserver.music.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;
    @Autowired
    public AdminService(PasswordEncoder passwordEncoder,AdminRepository adminRepository) {
        this.passwordEncoder = passwordEncoder;
        this.adminRepository = adminRepository;
    }
    private void saveAdminToDatabase(String username, String rawPassword) {
        // admin 테이블에 암호화된 비밀번호 저장 (예: JPA, JDBC 사용)
        // 예시로 가상의 코드로 설명
        String encryptedPassword = passwordEncoder.encode(rawPassword);
        Admin admin = new Admin(username, encryptedPassword);
        adminRepository.save(admin);
    }

    public boolean checkPassword(String rawPassword, String storedEncryptedPassword) {
        return passwordEncoder.matches(rawPassword, storedEncryptedPassword);
    }
    
    public boolean adminLogin(String userId, String rawPassword){
        Optional<Admin> admin = adminRepository.findByUserId(userId);
        if (admin.isPresent()) {
            //검증값 반환
            return checkPassword(
                    rawPassword,admin.get().getPassword()
            );
        } else {
            throw new RuntimeException("Admin not found with userId: " + userId);
        }
    }

    public Long getAdminIdLong(String userId){
        return adminRepository.findByUserId(userId).get().getId();
    }
}
