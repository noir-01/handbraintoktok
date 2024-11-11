package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.dto.MusicDto;
import com.example.handbrainserver.music.entity.Music;
import com.example.handbrainserver.music.repository.MusicRepository;
import com.example.handbrainserver.music.util.BeatExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MusicService {
    private final MusicRepository musicRepository;
    @Autowired
    public MusicService(MusicRepository musicRepository){
        this.musicRepository = musicRepository;
    }
    public List<MusicDto> getAllMusic() {
        List<MusicDto> musicList = musicRepository.findAllMusicWithoutFilePathAndBeatList();
        return musicRepository.findAll().stream() // Music 리스트를 스트림으로 변환
                .map(MusicDto::from) // 각 Music 객체를 MusicDto로 변환
                .collect(Collectors.toList());
    }

    public String getFilePathById(Long musicId){
        Optional<Music> music = musicRepository.findById(musicId);
        return music.map(Music::getFilePath).orElse(null);
    }

    public Long saveMusic(MusicDto musicDto){
        //beat를 추가해서 repo에 저장.
        String beatList = BeatExtractor.extractBeat(musicDto.getFilePath());
        MusicDto.MusicWithBeatListDto musicWithBeatListDto = new MusicDto.MusicWithBeatListDto(
                musicDto.getId(), musicDto.getTitle(), musicDto.getArtist(), musicDto.getDuration(), musicDto.getFilePath(), beatList
        );
        Music music = musicRepository.save(musicWithBeatListDto.to());
        return music.getId();
    }

    public List<Float> getBeatListById(Long musicId){
        Optional<Music> music = musicRepository.findById(musicId);
        return music.map(Music::getBeatTime).orElse(null);
    }
}
