package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.RandomGameHistory;
import com.example.handbrainserver.music.util.GameType;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RandomGameHistoryRepository extends JpaRepository<RandomGameHistory, Long> {
    //30일, 12주, 12월 간 평균 데이터
    @Query("SELECT DATE(r.date), AVG(r.reactionTime) " +
            "FROM RandomGameHistory r " +
            "WHERE r.user.id = :userId AND r.gameType = :gameType " +
            "AND r.date >= :startDate " +
            "GROUP BY DATE(r.date) " +
            "ORDER BY DATE(r.date)")
    List<Tuple> findDailyAverages(Long userId, GameType gameType, LocalDate startDate);

    @Query("SELECT MIN(DATE(r.date)), AVG(r.reactionTime) " +
            "FROM RandomGameHistory r " +
            "WHERE r.user.id = :userId AND r.gameType = :gameType " +
            "AND r.date >= :startDate " +
            "GROUP BY YEAR(r.date), WEEK(r.date) " +
            "ORDER BY YEAR(r.date), WEEK(r.date)")
    List<Tuple> findWeeklyAverages(Long userId, GameType gameType, LocalDate startDate);

    @Query("SELECT MIN(DATE(r.date)), AVG(r.reactionTime) " +
            "FROM RandomGameHistory r " +
            "WHERE r.user.id = :userId AND r.gameType = :gameType " +
            "AND r.date >= :startDate " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m')")
    List<Tuple> findMonthlyAverages(Long userId, GameType gameType, LocalDate startDate);

    @Query("SELECT AVG(r.reactionTime) FROM RandomGameHistory r WHERE r.gameType = :gameType")
    Float findAverageReactionTimeByGameTypeAllAge(@Param("gameType") GameType gameType);

    @Query("""
        SELECT AVG(r.reactionTime) 
        FROM RandomGameHistory r
        JOIN r.user u
        WHERE r.gameType = :gameType 
          AND YEAR(CURRENT_DATE) - u.birthYear BETWEEN :minAge AND :maxAge
    """)
    Float findAverageReactionTimeByGameTypeAndAgeGroup(
            @Param("gameType") GameType gameType,
            @Param("minAge") int minAge,
            @Param("maxAge") int maxAge
    );
}
