package com.example.handbrainserver.music.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class PeriodAverageDataDto{
    private LocalDate startDate; // 기간의 시작 날짜
    private Float averageReactionTime;
    public PeriodAverageDataDto(LocalDate startDate, Float averageReactionTime){
        this.startDate=startDate;
        this.averageReactionTime=averageReactionTime;
    }
    public PeriodAverageDataDto(){}
}
