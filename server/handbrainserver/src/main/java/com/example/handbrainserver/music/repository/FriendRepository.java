package com.example.handbrainserver.music.repository;

import com.example.handbrainserver.music.entity.Friend;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    // 친구 관계 확인
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    // 특정 유저의 모든 친구 가져오기
    List<Friend> findByUserIdAndVisibleTrue(Long userId);

    //unlink 1: 내 id 친구에게 안보이게
    @Modifying
    @Transactional
    @Query("UPDATE Friend f SET f.visible = false WHERE f.friendId = :myId")
    void updateVisibilityToFalse(Long myId);

    //unlink 2: 나도 친구 안보이게(del)
    @Modifying
    @Transactional
    @Query("DELETE FROM Friend f WHERE f.userId = :myId")
    void deleteByUserId(Long myId);

    //link: 이전에 내 id 친구에게 있다면 친구에게 내 id 보이게
    @Modifying
    @Transactional
    @Query("UPDATE Friend f SET f.visible = true WHERE f.friendId = :myId")
    void updateVisibilityToTrue(Long myId);

    //탈퇴
    @Modifying
    @Transactional
    @Query("DELETE FROM Friend f WHERE f.friendId = :myId or f.userId = :myId")
    void quitByUserId(Long myId);

}