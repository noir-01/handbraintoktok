package com.example.handbrainserver.music.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "Friend",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "friend_id"})
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //방향성 중요. (1,2), (2,1) 쌍 존재 가능.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    public Friend(Long userId, Long friendId){
        this.userId = userId;
        this.friendId = friendId;
    }
}