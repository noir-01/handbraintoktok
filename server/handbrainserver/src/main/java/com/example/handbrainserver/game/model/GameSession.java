package com.example.handbrainserver.game.model;

import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.game.util.GesturePair;
import com.example.handbrainserver.music.util.GameType;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Setter @Getter
public class GameSession {

    private Long userId;
    private String sessionId;
    private long startTime;
    private GameType gameType;

    private int questionCounter;
    private GesturePair question;
    private GesturePair userAnswer;
    private int questionType;
    private int calcQuestion;

    private int reactionTimeSum = 0;
    private int questionNums;

    private boolean isTutorial=false;
    public boolean getIsTutorial(){return isTutorial;}
    public void setIsTutorial(boolean isTutorial){this.isTutorial=isTutorial;}
    
    Random random = new Random();

    public GameSession(String sessionId) {
        this.sessionId = sessionId;
        this.startTime = System.currentTimeMillis();

        //10문제 카운트
        this.questionNums = 10;
        this.questionCounter=questionNums;

        question = new GesturePair();
    }
    //출제 전 확인해야 함.
    public boolean isEnd(){
        return questionCounter<0;
    }
    public void nextProblem() {
        Gesture[] rspGestures = {Gesture.ROCK, Gesture.TWO, Gesture.FIVE};
        switch(gameType){
            case COPY:
                questionType=0;
                break;
            case RSP:
                questionType= random.nextInt(2)+1;
                break;
            case CALC:
                questionType=3;
                break;
            case RANDOM:
                questionType=random.nextInt(4);
                break;
        }
        switch(questionType){
            case 0: //따라하기
                nextCopyQuestion();
                break;

            case 1: //이기는,지는 가위바위보: 문제랑 이기는/지는만 알면 문제랑 답 비교해서 정답인지 알 수 있음. 정답 따로 저장 X
            case 2:
//                question.setFirst(rspGestures[random.nextInt(rspGestures.length)]);
//                question.setSecond(rspGestures[random.nextInt(rspGestures.length)]);
                nextRspQuestion();
                break;

            case 3: //숫자 계산
                //calcQuestion = random.nextInt(11);
                nextCalcQuestion();
                break;
        }
        questionCounter-=1;
    }

    public boolean isAnswer(GesturePair givenAnswer){
        return switch(questionType){
            case 0 -> {
                if (givenAnswer.getFirst() == Gesture.HEART_TWO_HANDS || givenAnswer.getSecond() == Gesture.HEART_TWO_HANDS) {
                    yield question.getFirst() == givenAnswer.getFirst() || question.getSecond() == givenAnswer.getSecond();
                } else {
                    yield question.getFirst() == givenAnswer.getFirst() && question.getSecond() == givenAnswer.getSecond();
                }
            }
            case 1 -> Gesture.rspWin(question,givenAnswer);
            case 2 -> Gesture.rspLose(question,givenAnswer);
            case 3 -> Gesture.calcHand(calcQuestion,givenAnswer);
            default -> false;
        };
    }

    public void nextCopyQuestion(){
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
        
        //이전 문제 정답이 이번 문제 정답이랑 같은 경우 다시 뽑아야 함
        if(userAnswer!=null && isAnswer(userAnswer) || (firstValue==0 || secondValue==0)){
            //한번 더 뽑고 종료
            System.out.println("one more");
            nextCopyQuestion();
        }
    }

    public void nextRspQuestion(){
        Gesture[] rspGestures = {Gesture.ROCK, Gesture.TWO, Gesture.FIVE};
        Gesture firstGesture = rspGestures[random.nextInt(rspGestures.length)];
        Gesture secondGesture= rspGestures[random.nextInt(rspGestures.length)];
        
        question.setFirst(rspGestures[random.nextInt(rspGestures.length)]);
        question.setSecond(rspGestures[random.nextInt(rspGestures.length)]);
        
        //유저의 이전 정답이 이번 타입에서 정답일 경우 다시 뽑기
        if(userAnswer!=null && isAnswer(userAnswer)){
            //한번 더 뽑고 종료
            nextRspQuestion();
        }
    }


    public void nextCalcQuestion(){
        calcQuestion = random.nextInt(11);
        if(userAnswer!=null && isAnswer(userAnswer)){
            nextCalcQuestion();
        }
    }

    public int rockSissorsPaper(){
        int prob = random.nextInt(3);
        Gesture[] rsp = {Gesture.ROCK,Gesture.TWO,Gesture.FIVE};
        return rsp[prob].getGestureCode();
    }
    public void addReactionTime(int reactionTime){
        this.reactionTimeSum += reactionTime;
    }
    public int getReactionTimeAverage(){
        return this.reactionTimeSum / this.questionNums;
    }

}
