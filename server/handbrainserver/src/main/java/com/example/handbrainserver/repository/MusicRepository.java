package com.example.handbrainserver.repository;

import com.example.handbrainserver.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicRepository extends JpaRepository<Music, Long> {
}
