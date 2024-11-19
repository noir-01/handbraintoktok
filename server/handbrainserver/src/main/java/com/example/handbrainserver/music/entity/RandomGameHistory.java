package com.example.handbrainserver.music.entity;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.music.util.GameType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Random;

@Entity
@Getter @Setter
public class RandomGameHistory {
    public RandomGameHistory(){}
    public RandomGameHistory(GameType gameType, Gesture gesture, Float reactionTime, LocalDate date){
        this.gameType = gameType;
        this.gesture = gesture;
        this.reactionTime = reactionTime;
        this.date= date;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    // 따라하기 에서만 gesture 넣기? 아님 다 넣기?
    @Enumerated(EnumType.STRING)
    private Gesture gesture;

    @Column(nullable = false)
    private Float reactionTime;

    @Column(nullable = false)
    private LocalDate date;

}
