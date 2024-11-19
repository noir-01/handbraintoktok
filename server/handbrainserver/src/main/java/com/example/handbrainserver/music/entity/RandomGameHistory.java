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
    public RandomGameHistory(GameType gameType, Integer reactionTime, LocalDate date){
        this.gameType = gameType;
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

    //밀리초 단위 반응속도
    @Column(nullable = false)
    private Integer reactionTime;

    @Column(nullable = false)
    private LocalDate date;

}
