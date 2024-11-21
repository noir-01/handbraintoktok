package com.example.handbrainserver.music.dto;

import com.example.handbrainserver.music.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter@NoArgsConstructor@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String name;

    public static UserDto from(User user){
        return new UserDto(user.getId(),user.getName());
    }
}
