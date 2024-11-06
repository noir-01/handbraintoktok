package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.dto.MusicDto;
import com.example.handbrainserver.music.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MusicRepository extends JpaRepository<Music, Long> {
    @Query("SELECT new com.example.handbrainserver.music.dto.MusicDto(m.id, m.title, m.artist, m.duration) FROM Music m WHERE m.id = :id")
    MusicDto findMusicWithoutBeatListAndFilePath(@Param("id") Long id);
    @Query("SELECT new com.example.handbrainserver.music.dto.MusicDto(m.id, m.title, m.artist, m.duration) FROM Music m")
    List<MusicDto> findAllMusicWithoutFilePathAndBeatList();

}
