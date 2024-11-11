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
            case ONE -> 1;
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
        if(gesturePair.hasSecond()) {
            return answer == (defaultIntMapping(gesturePair.getFirst()) + defaultIntMapping(gesturePair.getSecond()));
        }
        else {
            return answer == defaultIntMapping(gesturePair.getFirst());
        }
    }

    public static boolean rspWin(GesturePair q, GesturePair a){
        //1. 양 손  7맞춰야 함
        if(q.hasSecond() && a.hasSecond()){
            return _rspWin(q.getFirst(),a.getFirst()) && _rspWin(q.getSecond(),a.getSecond());
        }else{
            return _rspWin(q.getFirst(),a.getFirst());
        }
    }

    public static boolean _rspWin(Gesture question, Gesture answer) {
        return switch (question) {
            case ROCK -> answer == Gesture.FIVE;
            case FIVE -> answer == Gesture.TWO || answer == Gesture.V; // Scissors beat Paper
            case TWO, V -> answer == Gesture.ROCK;     // Rock beats Scissors
            default -> false;
        };
    }

    public static boolean rspLose(GesturePair q, GesturePair a){
        //1. 양 손 맞춰야 함
        if(q.hasSecond() && a.hasSecond()){
            return _rspLose(q.getFirst(),a.getFirst()) && _rspLose(q.getSecond(),a.getSecond());
        }else{
            return _rspLose(q.getFirst(),a.getFirst());
        }
    }

    public static boolean _rspLose(Gesture question, Gesture answer) {
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

