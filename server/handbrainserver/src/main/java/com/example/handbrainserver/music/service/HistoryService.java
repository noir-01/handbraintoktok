package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.dto.PeriodAverageDataDto;
import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.entity.RandomGameHistory;
import com.example.handbrainserver.music.entity.RhythmGameHistory;
import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.repository.RandomGameHistoryRepository;
import com.example.handbrainserver.music.repository.RhythmGameHistoryRepository;
import com.example.handbrainserver.music.repository.UserRepository;
import com.example.handbrainserver.music.util.GameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.Tuple;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoryService {
    private final RandomGameHistoryRepository randomGameHistoryRepo;
    private final RhythmGameHistoryRepository rhythmGameHistoryRepo;
    private final UserRepository userRepository;
    @Autowired
    public HistoryService(RandomGameHistoryRepository rgh, RhythmGameHistoryRepository rhgh, UserRepository userRepository){
        this.randomGameHistoryRepo = rgh;
        this.rhythmGameHistoryRepo = rhgh;
        this.userRepository = userRepository;
    }
    public void saveRandomGameHistory(HistoryDto.RandomGameHistoryDto randomGameHistoryDto){
        RandomGameHistory randomGameHistory = new RandomGameHistory();
        //null처리 필요
        randomGameHistory.setUser(userRepository.findById(randomGameHistoryDto.getUserId()).get());
        randomGameHistory.setGameType(randomGameHistoryDto.getGameType());
        randomGameHistory.setReactionTime(randomGameHistoryDto.getReactionTime());
        randomGameHistory.setDate(randomGameHistoryDto.getDate());
        randomGameHistoryRepo.save(randomGameHistory);
    }
    public void saveRhythmGameHistory(HistoryDto.RhythmGameHistoryDto rhythmGameHistoryDto){
        RhythmGameHistory rhythmGameHistory = new RhythmGameHistory();
        //null처리 필요
        rhythmGameHistory.setUser(userRepository.findById(rhythmGameHistoryDto.getUserId()).get());
        rhythmGameHistory.setDifficulty(rhythmGameHistoryDto.getDifficulty());
        rhythmGameHistory.setDate(rhythmGameHistoryDto.getDate());
        rhythmGameHistory.setScore(rhythmGameHistoryDto.getScore());
        rhythmGameHistory.setCombo(rhythmGameHistoryDto.getCombo());

        rhythmGameHistoryRepo.save(rhythmGameHistory);
    }
    public List<PeriodAverageDataDto> findRandomGameHistoryDaily(Long userId, GameType gameType){
        return randomGameHistoryRepo.findDailyAverages(userId, gameType, LocalDate.now().minusDays(30))
                .stream()
                .map(tuple -> new PeriodAverageDataDto(
                        tuple.get(0, java.sql.Date.class).toLocalDate(),  // 날짜
                        tuple.get(1, Double.class).floatValue()  // 평균 반응 시간
                ))
                .collect(Collectors.toList());
    }
    public List<PeriodAverageDataDto> findRandomGameHistoryWeekly(Long userId, GameType gameType) {
        return randomGameHistoryRepo.findDailyAverages(userId, gameType, LocalDate.now().minusWeeks(12))
                .stream()
                .map(tuple -> new PeriodAverageDataDto(
                        tuple.get(0, java.sql.Date.class).toLocalDate(),  // 날짜
                        tuple.get(1, Double.class).floatValue()  // 평균 반응 시간
                ))
                .collect(Collectors.toList());
    }
    public List<PeriodAverageDataDto> findRandomGameHistoryMonthly(Long userId, GameType gameType) {
        return randomGameHistoryRepo.findDailyAverages(userId, gameType, LocalDate.now().minusMonths(12))
                .stream()
                .map(tuple -> new PeriodAverageDataDto(
                        tuple.get(0, java.sql.Date.class).toLocalDate(),  // 날짜
                        tuple.get(1, Double.class).floatValue()  // 평균 반응 시간
                ))
                .collect(Collectors.toList());
    }
//
//    public List<PeriodAverageDataDto> findRhythmGameHistoryDaily(Long userId, GameType gameType){
//        return randomGameHistoryRepo.findDailyAverages(userId, gameType);
//    }
//    public List<PeriodAverageDataDto> findRhythmGameHistoryWeekly(Long userId, GameType gameType){
//        return randomGameHistoryRepo.findWeeklyAverages(userId, gameType, LocalDate.now().minusWeeks(12));
//    }
//    public List<PeriodAverageDataDto> findRhythmGameHistoryMontly(Long userId, GameType gameType){
//        return randomGameHistoryRepo.findMonthlyAverages(userId, gameType, LocalDate.now().minusMonths(12));
//    }

}
