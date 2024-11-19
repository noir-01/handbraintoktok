package com.example.handbrainserver.music.dto;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.music.entity.RandomGameHistory;
import com.example.handbrainserver.music.util.Difficulty;
import com.example.handbrainserver.music.util.GameType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

public class HistoryDto {
    @Getter @Setter @AllArgsConstructor
    public static class RandomGameHistoryDto{
        private Long userId;
        private GameType gameType;
        private Float reactionTime;
        private LocalDate date;
        public static RandomGameHistoryDto from(RandomGameHistory randomGameHistory){
            return new RandomGameHistoryDto(
                    randomGameHistory.getUser().getId(),
                    randomGameHistory.getGameType(),
                    randomGameHistory.getReactionTime(),
                    randomGameHistory.getDate()
            );
        }
    }
    @Getter @Setter
    public static class RhythmGameHistoryDto{
        private Long userId;
        private Integer combo;
        private Integer score;
        private Difficulty difficulty;
        private LocalDate date;
    }

}
