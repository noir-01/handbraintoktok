package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.entity.RandomGameHistory;
import com.example.handbrainserver.music.entity.RhythmGameHistory;
import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.repository.RandomGameHistoryRepository;
import com.example.handbrainserver.music.repository.RhythmGameHistoryRepository;
import com.example.handbrainserver.music.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        randomGameHistory.setGesture(randomGameHistoryDto.getGesture());
        randomGameHistory.setReactionTime(randomGameHistoryDto.getReactionTime());

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

//    public List<HistoryDto.RandomGameHistoryDto> findRandomGameHistoryByUser(UserDto userdto){
//        List<RandomGameHistory> randomGameHistoryList = randomGameHistoryRepo.findByUser(
//                userRepository.findById(userdto.getUserId()).get()
//        );
//    }
    


}
