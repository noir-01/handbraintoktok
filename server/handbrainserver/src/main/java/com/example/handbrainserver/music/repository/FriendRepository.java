package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 관계 확인
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    // 특정 유저의 모든 친구 가져오기
    List<Friend> findByUserId(Long userId);
}