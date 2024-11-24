package com.example.handbrainserver.music.controller;

import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.dto.PeriodAverageDataDto;
import com.example.handbrainserver.music.service.FriendService;
import com.example.handbrainserver.music.service.HistoryService;
import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.GameType;
import com.example.handbrainserver.music.util.JwtUtil;
import com.example.handbrainserver.music.util.Period;
import io.jsonwebtoken.Jwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class  HistoryController {
    private final HistoryService historyService;
    private final UserService userService;
    private final FriendService friendService;

    private final JwtUtil jwtUtil = new JwtUtil();
    @Autowired
    public HistoryController(HistoryService historyService,UserService userService,FriendService friendService){
        this.historyService = historyService;
        this.userService = userService;
        this.friendService = friendService;
    }


    @PostMapping("/history/random/upload")
    public ResponseEntity<?> uploadRandomGameHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam GameType gameType,
            @RequestParam Integer reactionTime,
            @RequestParam LocalDate date
    ){
        String processedToken = token.replace("Bearer ", ""); // Bearer 제거
        try{
            Long userId = Long.parseLong(jwtUtil.extractUsername(processedToken));
            HistoryDto.RandomGameHistoryDto randomGameHistoryDto = new HistoryDto.RandomGameHistoryDto(
                    userService.getUserById(userId),
                    gameType,reactionTime,date
            );
            historyService.saveRandomGameHistory(randomGameHistoryDto);
            return ResponseEntity.ok("success");
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error");
        }
    }
    @GetMapping("/history/random/get")
    public List<PeriodAverageDataDto> getRandomGameHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam GameType gameType,
            @RequestParam Period period
    ) {
        String processedToken = token.replace("Bearer ", ""); // Bearer 제거
        JwtUtil jwtUtil = new JwtUtil();
        Long userId = Long.parseLong(jwtUtil.extractUsername(processedToken));
        return switch(period){
            case DAILY-> historyService.findRandomGameHistoryDaily(userId,gameType);
            case WEEKLY -> historyService.findRandomGameHistoryWeekly(userId,gameType);
            case MONTHLY -> historyService.findRandomGameHistoryMonthly(userId,gameType);
        };
    }
    
    // 친구 목록 조회하고 랭킹 정보 받기 
    @GetMapping("/history/rhythm/get/{musicId}")
    public List<HistoryDto.RhythmGameHistoryDto> getRhythmGameRanking(
            @RequestHeader("Authorization") String token,
            @PathVariable("musicId") Long musicId
    ){
        String processedToken = token.replace("Bearer ", ""); // Bearer 제거
        Long userId = Long.parseLong(jwtUtil.extractUsername(processedToken));

        List<Long> userIds = friendService.getFriendIds(userId);
        //내 id도 추가
        userIds.add(userId);

        //return historyService.findAllUserRecordWeekly(musicId);
        return historyService.getFriendRecordWeekly(userIds,musicId);
    }

}
