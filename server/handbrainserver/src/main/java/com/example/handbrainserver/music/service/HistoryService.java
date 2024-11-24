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
import com.example.handbrainserver.music.util.Difficulty;
import com.example.handbrainserver.music.util.GameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.Tuple;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HistoryService {
    private final RandomGameHistoryRepository randomGameHistoryRepo;
    private final RhythmGameHistoryRepository rhythmGameHistoryRepo;
    private final UserRepository userRepository;
    @Autowired
    public HistoryService(RandomGameHistoryRepository rgh, RhythmGameHistoryRepository rhgh, UserRepository userRepository, FriendService friendService){
        this.randomGameHistoryRepo = rgh;
        this.rhythmGameHistoryRepo = rhgh;
        this.userRepository = userRepository;
    }
    public void saveRandomGameHistory(HistoryDto.RandomGameHistoryDto randomGameHistoryDto){
        RandomGameHistory randomGameHistory = new RandomGameHistory();
        //null처리 필요
        randomGameHistory.setUser(userRepository.findById(randomGameHistoryDto.getUserDto().getUserId()).get());
        randomGameHistory.setGameType(randomGameHistoryDto.getGameType());
        randomGameHistory.setReactionTime(randomGameHistoryDto.getReactionTime());
        randomGameHistory.setDate(randomGameHistoryDto.getDate());
        randomGameHistoryRepo.save(randomGameHistory);
    }
    public void saveRhythmGameHistory(HistoryDto.RhythmGameHistoryDto rhythmGameHistoryDto){
        RhythmGameHistory rhythmGameHistory = new RhythmGameHistory();
        //null처리 필요
        rhythmGameHistory.setUser(userRepository.findById(rhythmGameHistoryDto.getUserDto().getUserId()).get());
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

    //dto 입력받아서 최고값이면 update or not
    public void updateWeeklyRecord(HistoryDto.RhythmGameHistoryDto rhythmGameHistoryDto) {
        Long userId = rhythmGameHistoryDto.getUserDto().getUserId();
        Long musicId = rhythmGameHistoryDto.getMusicId();
        Integer newScore = rhythmGameHistoryDto.getScore();
        Integer newCombo = rhythmGameHistoryDto.getCombo();
        Difficulty difficulty=rhythmGameHistoryDto.getDifficulty();
        LocalDate date=rhythmGameHistoryDto.getDate();

        User user = userRepository.findById(userId).get();
        // 주간 시작일과 종료일 계산
        LocalDate startOfWeek = getWeekStart(date);
        LocalDate endOfWeek = getWeekEnd(date);

        // 기존 주간 최고 기록 조회
        Optional<RhythmGameHistory> existingRecord = rhythmGameHistoryRepo.findMyTopRecordWeekly(user, musicId, startOfWeek, endOfWeek);

        if (existingRecord.isPresent()) {
            // 기존 기록이 있다면, 새로운 점수가 더 높은 경우 업데이트
            RhythmGameHistory record = existingRecord.get();
            if (newScore > record.getScore()) {
                record.setScore(newScore);
                record.setCombo(newCombo);
                record.setDifficulty(difficulty);
                record.setDate(date);
                rhythmGameHistoryRepo.save(record);
            }
        } else {
            // 기존 기록이 없다면 새로 저장
            RhythmGameHistory newRecord = new RhythmGameHistory();
            newRecord.setUser(user);
            newRecord.setScore(newScore);
            newRecord.setCombo(newCombo);
            newRecord.setDifficulty(difficulty);
            newRecord.setDate(date);
            rhythmGameHistoryRepo.save(newRecord);
        }
    }

    public List<HistoryDto.RhythmGameHistoryDto> getAllUserRecordWeekly(Long musicId){
        LocalDate startOfWeek = getWeekStart(LocalDate.now());
        LocalDate endOfWeek = getWeekEnd(LocalDate.now());
        return rhythmGameHistoryRepo.findAllUserRecordWeekly(musicId, startOfWeek,endOfWeek).stream()
                .map(HistoryDto.RhythmGameHistoryDto::from)
                .collect(Collectors.toList());
    }

    public List<HistoryDto.RhythmGameHistoryDto> getFriendRecordWeekly(List<Long> userIds, Long musicId) {

        LocalDate startOfWeek = getWeekStart(LocalDate.now());
        LocalDate endOfWeek = getWeekEnd(LocalDate.now());


        return rhythmGameHistoryRepo.findFriendRecordWeekly(musicId, userIds, startOfWeek, endOfWeek).stream()
                .map(HistoryDto.RhythmGameHistoryDto::from)
                .collect(Collectors.toList());
    }

    public LocalDate getWeekStart(LocalDate date) {
        // 해당 날짜가 포함된 주의 월요일
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public LocalDate getWeekEnd(LocalDate date) {
        // 해당 날짜가 포함된 주의 일요일
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

}
