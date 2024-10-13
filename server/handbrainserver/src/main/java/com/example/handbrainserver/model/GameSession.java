package com.example.handbrainserver.model;

public class GameSession {

    private String userId;
    private String sessionId;
    private long startTime;
    private boolean isRandomGame;

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.startTime = System.currentTimeMillis();
    }
    public void setUserId(String userId){
        this.userId=userId;
    }

    public void nextProblem() {
        this.startTime = System.currentTimeMillis();  // 새로운 문제 시작 시 시간 기록
        // 문제 출제 로직 (예: 무작위 문제 생성)
    }

    public void calculateReactionTime() {
        long reactionTime = System.currentTimeMillis() - startTime;
        System.out.println("사용자 " + userId + "의 반응속도: " + reactionTime + "ms");
        // 반응 속도 저장 로직 (DB 저장)
    }
}
