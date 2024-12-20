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
    private String phoneNumberHashed;
    private Integer birthYear;

    public static UserDto from(User user){
        return new UserDto(user.getId(),user.getName(),user.getPhoneNumberHash(), user.getBirthYear());
    }

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class UserDtoWithOutId{
        private String name;
        private String phoneNumber;
        private Integer birthYear;
    }
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class UserDtoWithIdAndName{
        private Long userId;
        private String name;
    }
}
