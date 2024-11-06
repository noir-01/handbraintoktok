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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.util.List;

@RestController
public class MusicController {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final MusicService musicService;
    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/music/getAllMusic")
    public List<MusicDto> getMusicList(){
        return musicService.getAllMusic();
    }

    @GetMapping("/music/download/{musicId}")
    public ResponseEntity<Resource> downloadMusicById(@PathVariable Long musicId){
        String filePath = musicService.getFilePathById(musicId);
        if (filePath == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping("/admin/upload")
    public ResponseEntity<String> uploadMusic(@RequestParam("title") String title,
                                              @RequestParam("artist") String artist,
                                              @RequestParam("duration") String duration,
                                              @RequestParam("file") MultipartFile file) {
        try {
            // 파일 확장자 확인
            if (!file.getOriginalFilename().endsWith(".mp3")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only .mp3 files are allowed.");
            }

            // 파일 저장 경로 설정
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + File.separator + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            //음악 메타데이터 저장
            MusicDto musicDto = new MusicDto(title,artist,LocalTime.parse(duration),filePath.toString());
            musicService.saveMusic(musicDto);

            return ResponseEntity.ok("Music uploaded successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving file.");
        }
    }
}