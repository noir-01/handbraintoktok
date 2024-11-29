package com.example.handbrainserver.game.util;

public enum Gesture {
    MIDDLE_FINGER(0),
    HEART(1),
    HEART_TWO_HANDS(2),
    THUMB_UP(3),
    V(4),
    OK(5),
    CALL(6),
    ALIEN(7),
    BABY(8),
    FOUR(9),
    MANDOO(10),
    ONE(11),
    RABBIT(12),
    ROCK(13),
    THREE(14),
    TWO(15),
    EIGHT(16),
    FIVE(17),
    LUCKY_FINGER(18),
    SEVEN(19),
    SIX(20),
    WOLF(21);

    private final int gestureCode;

    Gesture(int gestureCode) {
        this.gestureCode = gestureCode;
    }

    public int getGestureCode() {
        return gestureCode;
    }
    public static int defaultIntMapping(Gesture g){
        return switch(g){
            case ROCK -> 0;
            case ONE,THUMB_UP -> 1;
            case V,TWO -> 2;
            case THREE, SIX -> 3;
            case FOUR -> 4;
            case FIVE -> 5;
            default -> -1;
        };
    }

    public static int mappingSignLanguage(Gesture g){
        return switch(g){
            case MANDOO -> 0;
            case ONE -> 1;
            case TWO -> 2;
            case SIX -> 3;
            case FOUR -> 4;
            case THUMB_UP -> 5;
            case V -> 6;
            case THREE -> 7;
            case EIGHT -> 8;
            case FIVE -> 9;
            case OK -> 10;
            default -> -1;
        };
    }

    public static boolean calcHand(int answer, GesturePair gesturePair){
        //양손 모두 있을 때
        if(gesturePair.hasFirst()&&gesturePair.hasSecond()) {
            return answer == (defaultIntMapping(gesturePair.getFirst()) + defaultIntMapping(gesturePair.getSecond()));
        }
        else {
            if(gesturePair.hasFirst()){
                return answer == defaultIntMapping(gesturePair.getFirst());
            }else if (gesturePair.hasSecond()){
                return answer == defaultIntMapping(gesturePair.getSecond());
            }else{
                return false;
            }
        }
    }

    public static boolean rspWin(GesturePair q, GesturePair a){
        //양손 문제일 경우 둘다 확인
        if(q.hasFirst()&& q.hasSecond()){
            return _rspWin(q.getFirst(),a.getFirst()) && _rspWin(q.getSecond(),a.getSecond());
        }else{
            if(q.hasFirst())
                return _rspWin(q.getFirst(),a.getFirst());
            else if(q.hasSecond())
                return _rspWin(q.getSecond(),a.getSecond());
            else return false;
        }
    }

    public static boolean _rspWin(Gesture question, Gesture answer) {
        if(answer==null) return false;

        return switch (question) {
            case ROCK -> answer == Gesture.FIVE;
            case FIVE -> answer == Gesture.TWO || answer == Gesture.V; // Scissors beat Paper
            case TWO, V -> answer == Gesture.ROCK;     // Rock beats Scissors
            default -> false;
        };
    }

    public static boolean rspLose(GesturePair q, GesturePair a){
        //양손 문제일 경우 둘다 확인
        if(q.hasFirst()&& q.hasSecond()){
            return _rspLose(q.getFirst(),a.getFirst()) && _rspLose(q.getSecond(),a.getSecond());
        }else{
            if(q.hasFirst())
                return _rspLose(q.getFirst(),a.getFirst());
            else if(q.hasSecond())
                return _rspLose(q.getSecond(),a.getSecond());
            else return false;
        }
    }

    public static boolean _rspLose(Gesture question, Gesture answer) {
        if(answer==null) return false;
        return switch (question) {
            case ROCK -> answer == Gesture.TWO || answer == Gesture.V;
            case FIVE -> answer == Gesture.ROCK;
            case TWO, V -> answer == Gesture.FIVE;
            default -> false;
        };
    }

    public static Gesture fromCode(int code) {
        for (Gesture gesture : Gesture.values()) {
            if (gesture.getGestureCode() == code) {
                return gesture;
            }
        }
        throw new IllegalArgumentException("Invalid gesture code: " + code);
    }
}

