package com.example.handbrainserver.service;

import com.example.handbrainserver.dto.MusicDto;
import com.example.handbrainserver.entity.Music;
import com.example.handbrainserver.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MusicService {
    @Autowired
    private MusicRepository musicRepository;
    public List<MusicDto> getAllMusic() {
        List<Music> musicList = musicRepository.findAll();
        return musicRepository.findAll().stream() // Music 리스트를 스트림으로 변환
                .map(MusicDto::from) // 각 Music 객체를 MusicDto로 변환
                .collect(Collectors.toList());
    }


}
