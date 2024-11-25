package com.example.handbrainserver.music.service;

import com.example.handbrainserver.music.dto.UserDto;
import com.example.handbrainserver.music.entity.User;
import com.example.handbrainserver.music.repository.UserRepository;
import com.example.handbrainserver.music.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private CryptoUtil cryptoUtil = new CryptoUtil();
    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Long saveUser(UserDto.UserDtoWithOutId userDtoWithOutId){
        User user = new User();
        user.setName(userDtoWithOutId.getName());
        try {
            user.setPhoneNumberHash(cryptoUtil.encrypt(userDtoWithOutId.getPhoneNumber()));
            user = userRepository.save(user);
            return user.getId();

        }catch(Exception e){
            e.printStackTrace();
            return -1L;
        }
    }
    public UserDto getUserById(Long userId){
        return UserDto.from(userRepository.findById(userId).get());
    }
}
