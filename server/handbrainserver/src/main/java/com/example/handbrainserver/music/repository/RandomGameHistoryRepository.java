package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.RandomGameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RandomGameHistoryRepository extends JpaRepository<RandomGameHistory, Long> {
}
