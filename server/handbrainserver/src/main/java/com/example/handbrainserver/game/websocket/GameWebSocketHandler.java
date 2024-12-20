package com.example.handbrainserver.game.websocket;

import com.example.handbrainserver.game.model.GameSession;
import com.example.handbrainserver.game.util.Gesture;
import com.example.handbrainserver.game.util.GesturePair;
import com.example.handbrainserver.music.dto.HistoryDto;
import com.example.handbrainserver.music.service.HistoryService;
import com.example.handbrainserver.music.service.UserService;
import com.example.handbrainserver.music.util.GameType;
import com.example.handbrainserver.music.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.ConcurrentHashMap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    //sessionId, GameSession
    private final Map<String, GameSession> gameSessions = new ConcurrentHashMap<>();
    
    private JwtUtil jwtUtil = new JwtUtil();
    @Autowired
    private UserService userService;
    @Autowired
    private HistoryService historyService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        gameSessions.put(sessionId, new GameSession(sessionId));
        session.sendMessage(new TextMessage("input id and isRandom"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GameSession gameSession = gameSessions.get(session.getId());

        String payload = message.getPayload();

        if (gameSession!=null && gameSession.getUserId() == null) {
            // JSON 메시지 파싱
            String[] inputs = payload.split(","); // ","로 구분된 문자열로 입력을 처리

            if (inputs.length == 2) { // 두 개의 값이 있는 경우
                //처음에 token 전송으로 유저 찾아야 함.
                String token = inputs[0].trim(); // 첫 번째 값: token, token=>id
                Long userId = Long.parseLong(jwtUtil.extractUserId(token));
                System.out.println("userId: "+userId.toString());

                String gameTypeString = inputs[1].trim();
                switch(gameTypeString){
                    case "COPY":
                        gameSession.setGameType(GameType.COPY);
                        break;
                    case "RSP":
                        gameSession.setGameType(GameType.RSP);
                        break;
                    case "CALC":
                        gameSession.setGameType(GameType.CALC);
                        break;
                    case "RANDOM":
                        gameSession.setGameType(GameType.RANDOM);
                        break;
                }

                gameSession.setUserId(userId);
                session.sendMessage(new TextMessage("게임 시작! 첫 문제를 기다리세요."));

                //첫문제 출제.
                gameSession.nextProblem();
                //gameMessage 만들어서 전송
                session.sendMessage(new TextMessage(nextGameMessage(gameSession)));

            } else {
                //튜토리얼은 처음에 3개 보내기
                String token = inputs[0].trim(); // 첫 번째 값: token, token=>id
                Long userId = Long.parseLong(jwtUtil.extractUserId(token));
                System.out.println("userId: "+userId.toString());
                gameSession.setUserId(userId);
                gameSession.setIsTutorial(true);
                //첫문제: 따라하기 출제
                gameSession.setGameType(GameType.COPY);
                //따라하기 - 가위바위보 이기기 - 지기 - 계산하기 순서대로 4문제 출제
                gameSession.setQuestionNums(4);
                gameSession.nextProblem();
                session.sendMessage(new TextMessage(nextGameMessage(gameSession)));
            }

        } else {
            // 이후 메시지: 사용자 응답을 문자열로 처리
            assert gameSession != null;
            handleGameMessage(session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 종료 처리
        String sessionId = session.getId();
        gameSessions.remove(sessionId);
    }

    public void handleGameMessage(WebSocketSession session, String payload) {
        GameSession gameSession = gameSessions.get(session.getId());
        
        //유저는 firstAnser,secondAnswer,reactionTime 전송
        String[] inputs = payload.split(",");
        GesturePair userAnswer = new GesturePair();
        
        Integer firstAnswer = !inputs[0].equals("-1")?Integer.parseInt(inputs[0]):null;
        Integer secondAnswer = !inputs[1].equals("-1")?Integer.parseInt(inputs[1]):null;
        Integer reactionTime = Integer.parseInt(inputs[2]);

        if(firstAnswer!=null){
            userAnswer.setFirst(Gesture.fromCode(firstAnswer));
        }
        if(secondAnswer!=null){
            userAnswer.setSecond(Gesture.fromCode(secondAnswer));
        }
        // 정답 판단 및 문제 출제
        boolean isCorrect = gameSession.isAnswer(userAnswer);
        gameSession.setUserAnswer(userAnswer);
        try {
            if (isCorrect) {
                // 정답일 경우 반응속도 더해놓기 (나중에 평균내서 저장)
                gameSession.addReactionTime(reactionTime);

                if(gameSession.getIsTutorial()){
                    switch(gameSession.getQuestionCounter()){
                        case 3: //이기는 가위바위보
                            gameSession.setGameType(GameType.RSP);
                            gameSession.setQuestionType(1);
                            gameSession.nextRspQuestion();
                            gameSession.setQuestionCounter(2);
                            break;
                        case 2: //지는 가위바위보
                            gameSession.setGameType(GameType.RSP);
                            gameSession.setQuestionType(2);
                            gameSession.nextRspQuestion();
                            gameSession.setQuestionCounter(1);
                            break;
                        case 1: //계산 (끝)
                            gameSession.setGameType(GameType.CALC);
                            gameSession.nextCalcQuestion();
                            gameSession.setQuestionCounter(0);
                            break;
                        case 0:
                            gameSession.setQuestionCounter(-1);
                            break;
                    }
                }else{
                    gameSession.nextProblem();
                }

                session.sendMessage(new TextMessage("correct"));

                //마지막 문제면 반응속도 기록
                if (gameSession.isEnd()) {
                    HistoryDto.RandomGameHistoryDto historyDto = new HistoryDto.RandomGameHistoryDto();
                    historyDto.setUserDto(
                            userService.getUserById(gameSession.getUserId())
                    );
                    historyDto.setGameType(
                            gameSession.getGameType()
                    );
                    historyDto.setDate(LocalDate.now());
                    historyDto.setReactionTime(
                            gameSession.getReactionTimeAverage())
                    ;
                    historyService.saveRandomGameHistory(historyDto);

                    session.sendMessage(new TextMessage("end,"+Integer.toString(gameSession.getReactionTimeAverage())));
                } else {
                    //gameMessage 만들어서 전송
                    session.sendMessage(new TextMessage(nextGameMessage(gameSession)));
                }
            }
        } catch (IOException e) {
            // 오류 처리 로직 (예: 로그 기록, 클라이언트 연결 종료 등)
            System.err.println("메시지 전송 중 오류 발생: " + e.getMessage());
        }
    }

    public String nextGameMessage(GameSession gameSession){
        int questionType = gameSession.getQuestionType();
        String message;

        // 문제 타입에 따라 전송할 내용을 결정합니다.
        if (questionType == 3) {
            // 문제 타입이 3인 경우 계산 문제를 가져옵니다.
            int calcQuestion = gameSession.getCalcQuestion();
            message = String.format("next: %d,%d,null", questionType, calcQuestion);
        } else {
            GesturePair gesturePair = gameSession.getQuestion();
            message = String.format("next: %d,%s,%s", questionType, gesturePair.getFirst().getGestureCode(),
                    gesturePair.getSecond() != null ? gesturePair.getSecond().getGestureCode() : "null");
        }
        return message;
    }

}
