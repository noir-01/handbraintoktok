package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicRepository extends JpaRepository<Music, Long> {
}
