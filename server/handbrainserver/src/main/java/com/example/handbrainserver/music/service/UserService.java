package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public Long saveUser(String phoneNumber){
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user = userRepository.save(user);
        return user.getId();
    }
    public UserDto getUserById(Long userId){
        return UserDto.from(userRepository.findById(userId).get());
    }
}
