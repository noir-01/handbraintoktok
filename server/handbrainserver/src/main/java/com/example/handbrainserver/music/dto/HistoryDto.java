package com.example.handbrainserver.music.dto;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.music.entity.RandomGameHistory;
import com.example.handbrainserver.music.entity.RhythmGameHistory;
import com.example.handbrainserver.music.util.Difficulty;
import com.example.handbrainserver.music.util.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

public class HistoryDto {
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class RandomGameHistoryDto{
        private UserDto userDto;
        private GameType gameType;
        private Integer reactionTime;
        private LocalDate date;
        public static RandomGameHistoryDto from(RandomGameHistory randomGameHistory){
            return new RandomGameHistoryDto(
                    UserDto.from(randomGameHistory.getUser()),
                    randomGameHistory.getGameType(),
                    randomGameHistory.getReactionTime(),
                    randomGameHistory.getDate()
            );
        }
    }
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class RhythmGameHistoryDto{
        private UserDto userDto;
        private Long musicId;
        private Integer combo;
        private Integer score;
        private Difficulty difficulty;
        private LocalDate date;

        public static RhythmGameHistoryDto from(RhythmGameHistory rhythmGameHistory){
            UserDto userDto = UserDto.from(rhythmGameHistory.getUser());
            //개인정보 가리고 보내기
            userDto.setBirthYear(0);
            userDto.setPhoneNumberHashed("");
            return new RhythmGameHistoryDto(
                    userDto,
                    rhythmGameHistory.getMusic().getId(),
                    rhythmGameHistory.getCombo(),
                    rhythmGameHistory.getScore(),
                    rhythmGameHistory.getDifficulty(),
                    rhythmGameHistory.getDate()
            );
        }
    }
}
