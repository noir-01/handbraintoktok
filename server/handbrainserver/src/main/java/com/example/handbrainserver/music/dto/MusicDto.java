package com.example.handbrainserver.music.dto;

import com.example.handbrainserver.music.entity.Music;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicDto {
    private Long id;
    private String title;
    private String artist;
    private LocalTime duration;
    private String filePath;
    public MusicDto(String title, String artist, LocalTime duration, String filePath){
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
    }
    public static MusicDto from(Music music){
        MusicDto musicDto = new MusicDto();
        musicDto.setId(music.getId());
        musicDto.setTitle(music.getTitle());
        musicDto.setArtist(music.getArtist());
        musicDto.setDuration(music.getDuration());
        return musicDto;
    }
    public Music to(){
        Music music = new Music();
        music.setTitle(this.title);
        music.setArtist(this.artist);
        music.setDuration(this.duration);
        music.setFilePath(this.artist);
        return music;
    }
}
