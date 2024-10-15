package com.example.handbrainserver.websocket;

import com.example.handbrainserver.model.GameSession;
import com.example.handbrainserver.util.Gesture;
import com.example.handbrainserver.util.GesturePair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    //sessionId, GameSession
    private final Map<String, GameSession> gameSessions = new HashMap<>();

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

        if (gameSession.getUserId() == null) {
            // JSON 메시지 파싱
            String[] inputs = payload.split(","); // ":"로 구분된 문자열로 입력을 처리

            if (inputs.length == 2) { // 두 개의 값이 있는 경우
                String userId = inputs[0].trim(); // 첫 번째 값: userId
                boolean isRandomGame = Boolean.parseBoolean(inputs[1].trim());

                gameSession.setUserId(userId);
                gameSession.setRandomGame(isRandomGame);
                session.sendMessage(new TextMessage("게임 시작! 첫 문제를 기다리세요."));

                //첫문제 출제.
                gameSession.nextProblem();
                //gameMessage 만들어서 전송
                session.sendMessage(new TextMessage(nextGameMessage(gameSession)));

            } else {

                session.sendMessage(new TextMessage("잘못된 첫 번째 메시지입니다."));
            }

        } else {
            // 이후 메시지: 사용자 응답을 문자열로 처리
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

        // 예측한 정답을 정수형으로 변환
        String[] inputs = payload.split(",");
        GesturePair userAnswer = new GesturePair();

        userAnswer.setFirst(Gesture.fromCode(Integer.parseInt(inputs[0])));
        Integer secondAnswer = inputs.length > 1 && !inputs[1].equals("null") ? Integer.parseInt(inputs[1]) : null;
        if(secondAnswer!=null){
            userAnswer.setSecond(Gesture.fromCode(secondAnswer));
        }

        // 정답 판단 및 문제 출제
        boolean isCorrect = gameSession.isAnswer(userAnswer);

        try {
            if (isCorrect) {
                // 정답일 경우 다음 문제 출제
                gameSession.nextProblem();
                session.sendMessage(new TextMessage("correct"));

                if (gameSession.isEnd()) {
                    session.sendMessage(new TextMessage("end"));
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
            message = String.format("next: %d,%s,null", questionType, calcQuestion);
        } else {
            GesturePair gesturePair = gameSession.getQuestion();
            message = String.format("next: %d,%s,%s", questionType, gesturePair.getFirst().getGestureCode(),
                    gesturePair.getSecond() != null ? gesturePair.getSecond().getGestureCode() : "null");
        }
        return message;
    }

}
