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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final MusicService musicService;
    @Autowired
    public RestController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/music/getMusicList")
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

}
