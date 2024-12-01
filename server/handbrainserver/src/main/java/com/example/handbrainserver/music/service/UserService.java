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
            String phoneNumberHash = cryptoUtil.encrypt(userDtoWithOutId.getPhoneNumber());
            //이미 존재하는 유저면 -1 반환
            if(userRepository.existsUserByPhoneNumberHash(phoneNumberHash)){
                return -1L;
            }
            user.setPhoneNumberHash(phoneNumberHash);
            user = userRepository.save(user);
            return user.getId();

        }catch(Exception e){
            e.printStackTrace();
            return -2L;
        }
    }
    public UserDto getUserById(Long userId){
        return UserDto.from(userRepository.findById(userId).get());
    }
    public UserDto getUserByPhone(String phoneNumber){
        try{
            String phoneHash = cryptoUtil.encrypt(phoneNumber);
            return UserDto.from(userRepository.findByPhoneNumberHash(phoneHash).get());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
