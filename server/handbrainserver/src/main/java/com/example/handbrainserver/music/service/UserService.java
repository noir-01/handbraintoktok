package com.example.handbrainserver.music.service;

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
    public boolean saveUser(String phoneNumber){
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);
        return true;
    }
}
