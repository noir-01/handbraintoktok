package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.dto.PeriodAverageDataDto;
import com.example.handbrainserver.music.entity.RhythmGameHistory;
import com.example.handbrainserver.music.util.Difficulty;
import com.example.handbrainserver.music.util.GameType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RhythmGameHistoryRepository extends JpaRepository<RhythmGameHistory, Long> {
    @Query("SELECT DATE(r.date), AVG(r.score) " +
            "FROM RhythmGameHistory r " +
            "WHERE r.user.id = :userId AND r.difficulty = :difficulty " +
            "AND r.date >= :startDate " +
            "GROUP BY DATE(r.date) " +
            "ORDER BY DATE(r.date)")
    List<Tuple> findDailyAverages(Long userId, Difficulty difficulty, LocalDate startDate);
//
//    @Query("SELECT new com.example.handbrainserver.music.dto.PeriodAverageDataDto(MIN(DATE(r.date)), AVG(r.reactionTime)) " +
//            "FROM RhythmGameHistory r " +
//            "WHERE r.user.id = :userId AND r.gameType = :gameType " +
//            "AND r.date >= :startDate " +
//            "GROUP BY YEAR(r.date), WEEK(r.date) " +
//            "ORDER BY YEAR(r.date), WEEK(r.date)")
//    List<PeriodAverageDataDto> findWeeklyAverages(@Param("userId") Long userId,
//                                                             @Param("gameType") GameType gameType,
//                                                             @Param("startDate") LocalDate startDate);
//
//    @Query("SELECT new com.example.handbrainserver.music.dto.PeriodAverageDataDto(MIN(DATE(r.date)), AVG(r.reactionTime)) " +
//            "FROM RhythmGameHistory r " +
//            "WHERE r.user.id = :userId AND r.gameType = :gameType " +
//            "AND r.date >= :startDate " +
//            "GROUP BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m') " +
//            "ORDER BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m')")
//    List<PeriodAverageDataDto> findMonthlyAverages(@Param("userId") Long userId,
//                                                              @Param("gameType") GameType gameType,
//                                                              @Param("startDate") LocalDate startDate);
}
