package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.dto.MusicDto;
import com.example.handbrainserver.music.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.List;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
public class MusicController {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final MusicService musicService;
    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/admin/upload")
    public String uploadMusic(HttpSession session, Model model){
        String token = (String) session.getAttribute("token");
        model.addAttribute("token", token);
        return "musicUpload";
    }

    @CrossOrigin(origins = "https://handbraintoktok.duckdns.org:8080")
    @PostMapping("/admin/upload")
    public ResponseEntity<?> uploadMusic(@RequestParam("title") String title,
                                              @RequestParam("artist") String artist,
                                              @RequestParam("duration") String duration,
                                              @RequestParam("file") MultipartFile file) {
        try {
            // 파일 확장자 확인
            if (!file.getOriginalFilename().endsWith(".mp3")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Only .mp3 files are allowed."));
            }

            // 파일 저장 경로 설정
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + File.separator + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            //음악 메타데이터 저장
            MusicDto musicDto = new MusicDto(title,artist,LocalTime.parse(duration),uploadDir+"/"+fileName);
            Long id = musicService.saveMusic(musicDto);


            return ResponseEntity.ok(Map.of("success:","Music uploaded successfully."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Error saving file."));
        }
    }
}
