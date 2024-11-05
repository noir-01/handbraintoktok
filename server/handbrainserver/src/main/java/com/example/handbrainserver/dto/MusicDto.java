package com.example.handbrainserver.dto;

import com.example.handbrainserver.entity.Music;
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

    public static MusicDto from(Music music){
        MusicDto musicDto = new MusicDto();
        musicDto.setId(music.getId());
        musicDto.setTitle(music.getTitle());
        musicDto.setArtist(music.getArtist());
        musicDto.setDuration(music.getDuration());
        return musicDto;
    }
}
