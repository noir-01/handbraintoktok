package com.example.handbrainserver.game.model;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.game.util.GesturePair;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Setter
@Getter
public class GameSession {

    private String userId;
    private String sessionId;
    private long startTime;
    private boolean isRandomGame;

    private int questionCounter;
    private GesturePair question;
    private GesturePair correctAnswer;
    private int questionType;
    private int calcQuestion;
    
    Random random = new Random();

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.startTime = System.currentTimeMillis();

        //10문제 카운트
        this.questionCounter = 10;
        question = new GesturePair();
        correctAnswer = new GesturePair();
    }
    //출제 전 확인해야 함.
    public boolean isEnd(){
        return questionCounter<0;
    }
    public void nextProblem() {
        Gesture[] rspGestures = {Gesture.ROCK, Gesture.TWO, Gesture.FIVE};

        //기본 따라하기 - 랜덤으로 따라할 동작 2개 뽑아서 반환
        if(!isRandomGame){
            questionType = 0;
            getCopyQuestion();

        }else{
            //따라하기, 지는/이기는 가위바위보, 숫자 계산
            questionType = random.nextInt(4);
            switch(questionType){
                case 0: //따라하기
                    getCopyQuestion();
                    break;

                case 1: //지는/이기는 가위바위보
                case 2:
                    question.setFirst(rspGestures[random.nextInt(rspGestures.length)]);
                    question.setSecond(rspGestures[random.nextInt(rspGestures.length)]);
                    break;
                    
                case 3: //숫자 계산
                    calcQuestion = random.nextInt(11);
                    break;
            }
        }
        questionCounter-=1;
    }

    public boolean isAnswer(GesturePair userAnswer){
        return switch(questionType){
            case 0 -> (question.getFirst()==userAnswer.getFirst() && question.getSecond()==userAnswer.getSecond());
            case 1 -> Gesture.rspWin(question,userAnswer);
            case 2 -> Gesture.rspLose(question,userAnswer);
            case 3 -> Gesture.calcHand(calcQuestion,userAnswer);
            default -> false;
        };
    }

    private void getCopyQuestion(){
        int firstValue = random.nextInt(Gesture.values().length);
        int secondValue = random.nextInt(Gesture.values().length);

        if (firstValue == Gesture.HEART_TWO_HANDS.getGestureCode() || secondValue == Gesture.HEART_TWO_HANDS.getGestureCode()) {
          question.setFirst(Gesture.HEART_TWO_HANDS);
          question.setSecond(null);
        } else {
            // 둘 다 2가 아닌 경우
            question.setFirst(Gesture.fromCode(firstValue));
            question.setSecond(Gesture.fromCode(secondValue));
        }
        correctAnswer.setFirst(question.getFirst());
        correctAnswer.setSecond(question.getSecond());
    }

    public void calculateReactionTime() {
        long reactionTime = System.currentTimeMillis() - startTime;
        System.out.println("사용자 " + userId + "의 반응속도: " + reactionTime + "ms");
        // 반응 속도 저장 로직 (DB 저장)
    }
    public int rockSissorsPaper(){
        int prob = random.nextInt(3);
        Gesture[] rsp = {Gesture.ROCK,Gesture.TWO,Gesture.FIVE};
        return rsp[prob].getGestureCode();
    }

}
