package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.entity.Friend;
import com.example.handbrainserver.music.repository.FriendRepository;
import com.example.handbrainserver.music.repository.UserRepository;
import com.example.handbrainserver.music.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    @Autowired
    public FriendService(FriendRepository friendRepository, UserRepository userRepository){
        this.friendRepository = friendRepository;
        this.userRepository = userRepository;
    }

    public void updateFriends(Long userId, List<String> contacts){
        List<String> phoneHashes = contacts.stream()
                .map(phoneNumber -> {
                    try {
                        return CryptoUtil.encrypt(phoneNumber);
                    } catch (Exception e) {
                        return "";
                    }
                })
                .collect(Collectors.toList());

        //만약 친구 번호가 DB에 존재한다면 Friend Repo에 넣기
        List<User> matchedUsers = userRepository.findByPhoneNumberHashIn(phoneHashes);

        List<Friend> newFriends = matchedUsers.stream()
                .filter(user -> !friendRepository.existsByUserIdAndFriendId(userId, user.getId()))
                .map(user -> new Friend(userId, user.getId()))
                .collect(Collectors.toList());
        friendRepository.saveAll(newFriends);
    }
    public List<Long> getFriendIds(Long userId) {
        List<Friend> friends = friendRepository.findByUserId(userId);
        return friends.stream()
                .map(friend -> friend.getUserId().equals(userId) ? friend.getFriendId() : friend.getUserId())
                .collect(Collectors.toList());
    }
}
