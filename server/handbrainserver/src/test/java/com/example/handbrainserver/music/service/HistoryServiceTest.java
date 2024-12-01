// package com.example.handbrainserver.music.service;

// import com.example.handbrainserver.music.dto.HistoryDto;
// import com.example.handbrainserver.music.dto.PeriodAverageDataDto;
// import com.example.handbrainserver.music.dto.UserDto;
// import com.example.handbrainserver.music.util.GameType;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.time.LocalDate;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// @SpringBootTest
// public class HistoryServiceTest {
//     private final HistoryService historyService;
//     @Autowired
//     public HistoryServiceTest(HistoryService historyService){
//         this.historyService = historyService;
//     }
//     @Test
//     public void saveAndCheck(){
// //        HistoryDto.RandomGameHistoryDto randomGameHistoryDto = new HistoryDto.RandomGameHistoryDto(
// //                new UserDto(8L,"123"),
// //                GameType.COPY,
// //                200,
// //                LocalDate.now().minusDays(3)
// //        );
// //        historyService.saveRandomGameHistory(randomGameHistoryDto);
// //        randomGameHistoryDto = new HistoryDto.RandomGameHistoryDto(
// //                new UserDto(8L,"123"),
// //                GameType.COPY,
// //                300,
// //                LocalDate.now().minusDays(2)
// //        );
// //        historyService.saveRandomGameHistory(randomGameHistoryDto);
// //        randomGameHistoryDto = new HistoryDto.RandomGameHistoryDto(
// //                new UserDto(8L,"123"),
// //                GameType.COPY,
// //                400,
// //                LocalDate.now().minusDays(1)
// //        );
// //        historyService.saveRandomGameHistory(randomGameHistoryDto);
// //        int i =0;
// //        List<PeriodAverageDataDto> results =  historyService.findRandomGameHistoryDaily(8L,GameType.COPY);
// //        for(PeriodAverageDataDto r: results){
// //            //assertEquals(r.getAverageReactionTime(),200f+100f*(i++));
// //        }

//     }
// }
