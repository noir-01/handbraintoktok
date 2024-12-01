// package com.example.handbrainserver;

// import com.example.handbrainserver.music.dto.UserDto;
// import com.example.handbrainserver.music.service.FriendService;
// import com.example.handbrainserver.music.service.UserService;
// import jakarta.transaction.Transactional;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;

// import java.util.ArrayList;
// import java.util.List;

// @SpringBootTest
// @Transactional
// public class FriendTest {
//     @Autowired
//     private FriendService friendService;
//     @Autowired
//     private UserService userService;

//     @Test
//     public void updateFriendTest(){
// //        UserDto.UserDtoWithOutId userDto = new UserDto.UserDtoWithOutId();
// //
// //        userDto.setName("a");
// //        userDto.setPhoneNumber("111");
// //        userService.saveUser(userDto);
// //
// //        userDto.setName("b");
// //        userDto.setPhoneNumber("222");
// //        userService.saveUser(userDto);
// //
// //        userDto.setName("c");
// //        userDto.setPhoneNumber("333");
// //        userService.saveUser(userDto);

//         //ex) a가 b,c 연락처를 갖고 있음.
//         List<String> contacts = new ArrayList<>();
//         contacts.add("111");
//         contacts.add("333");
//         contacts.add("444");

//         friendService.updateFriends(13L,contacts);
//     }
// }
