package com.example.handbrainserver.websocket;

import com.example.handbrainserver.model.GameSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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
        session.sendMessage(new TextMessage("게임 시작! 첫 문제를 기다리세요."));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        GameSession gameSession = gameSessions.get(session.getId());

        String payload = message.getPayload();
        // 첫 번째 메시지로 사용자 ID를 받는 경우
        if (payload.startsWith("userId:")) {
            String userId = payload.replace("userId:", "").trim();
            gameSession.setUserId(userId);
            session.sendMessage(new TextMessage("게임 시작! 첫 문제를 기다리세요."));
            
        } else {
            // 이후 메시지는 사용자 응답으로 처리
            handleGameMessage(session, payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 세션 종료 처리
        String sessionId = session.getId();
        gameSessions.remove(sessionId);
    }
}
