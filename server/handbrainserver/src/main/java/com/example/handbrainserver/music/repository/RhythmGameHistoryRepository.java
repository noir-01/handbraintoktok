package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.dto.PeriodAverageDataDto;
import com.example.handbrainserver.music.entity.RhythmGameHistory;
import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.util.Difficulty;
import com.example.handbrainserver.music.util.GameType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RhythmGameHistoryRepository extends JpaRepository<RhythmGameHistory, Long> {
    //사용자의 주간 기록이 존재하는지 확인
    @Query("SELECT h FROM RhythmGameHistory h WHERE h.user = :user AND h.music.id = :musicId AND h.date BETWEEN :startOfWeek AND :endOfWeek")
    Optional<RhythmGameHistory> findMyTopRecordWeekly(
            User user, Long musicId, LocalDate startOfWeek, LocalDate endOfWeek);

    @Query("SELECT h FROM RhythmGameHistory h WHERE h.music.id = :musicId AND h.date BETWEEN :startOfWeek AND :endOfWeek ORDER BY h.score DESC")
    List<RhythmGameHistory> findAllUserRecordWeekly(
            Long musicId, LocalDate startOfWeek, LocalDate endOfWeek);
    
    //나+친구들 기록 뽑기
    @Query("SELECT h FROM RhythmGameHistory h WHERE h.music.id = :musicId " +
            "AND h.user.id IN :userIds " +
            "AND h.date BETWEEN :startOfWeek AND :endOfWeek " +
            "ORDER BY h.score DESC")
    List<RhythmGameHistory> findFriendRecordWeekly(
            Long musicId,
            List<Long> userIds,
            LocalDate startOfWeek,
            LocalDate endOfWeek);
}
