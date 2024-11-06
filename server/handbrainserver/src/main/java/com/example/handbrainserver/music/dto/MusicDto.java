package com.example.handbrainserver.music.dto;

import com.example.handbrainserver.music.entity.Music;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
// 유저 업로드 시 id/beatList 불필요
// 유저 get 시 beatList 불필요
// 게임 플레이 시만 beatList 필요
public class MusicDto {
    private Long id;
    private String title;
    private String artist;
    private LocalTime duration;
    private String filePath;
    public MusicDto(Long id, String title, String artist, LocalTime duration, String filePath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
    }
    public MusicDto(Long id, String title, String artist, LocalTime duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }
    @Setter @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MusicWithBeatListDto {
        private Long id;
        private String title;
        private String artist;
        private LocalTime duration;
        private String filePath;
        private String beatList;
        public Music to(){
            Music music = new Music();
            music.setTitle(this.title);
            music.setArtist(this.artist);
            music.setDuration(this.duration);
            music.setFilePath(this.filePath);
            music.setBeatList(this.beatList);
            return music;
        }
    }

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
}
