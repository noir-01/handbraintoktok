package com.example.handbrainserver.game.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GesturePair {
    private Gesture first;
    private Gesture second;

    public GesturePair(Gesture first, Gesture second) {
        this.first = first;
        this.second = second;  // 필요 없을 경우 null
    }
    public GesturePair(Gesture first) {
        this.first = first;
        this.second = null;  // 필요 없을 경우 null
    }
    public GesturePair(){
        this.first=null;
        this.second=null;
    }

    public boolean hasSecond() {
        return second != null;  // 두 번째 값이 있는지 확인하는 헬퍼 메서드
    }
    public boolean hasFirst() {return first!=null;}
}
