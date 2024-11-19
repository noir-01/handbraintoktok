package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.RhythmGameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhythmGameHistoryRepository extends JpaRepository<RhythmGameHistory, Long> {
}
