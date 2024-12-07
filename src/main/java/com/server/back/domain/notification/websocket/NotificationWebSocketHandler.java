package com.server.back.domain.notification.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.CloseStatus;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    @Value("${jwt.secret}")
    private String secretKey;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            // WebSocket의 Query Parameters에서 "token" 추출
            String query = session.getUri().getQuery();
            if (query == null || !query.contains("token=")) {
                log.error("WebSocket 연결 실패: 토큰 누락");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 안전하게 토큰 추출
            String token = Arrays.stream(query.split("&"))
                    .filter(param -> param.startsWith("token="))
                    .map(param -> param.substring(6))
                    .findFirst()
                    .orElse(null);

            if (token == null || token.isEmpty()) {
                log.error("WebSocket 연결 실패: 토큰 누락 또는 비어 있음");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 닉네임 추출 및 검증
            String nickname;
            try {
                nickname = extraNicknameFromToken(token);
            } catch (Exception e) {
                log.error("WebSocket 연결 실패: 토큰 검증 중 오류 발생", e);
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            if (nickname == null || nickname.isEmpty()) {
                log.error("WebSocket 연결 실패: 유효하지 않은 토큰");
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }

            // 기존 세션 확인 및 닫기
            WebSocketSession existingSession = sessions.get(nickname);
            if (existingSession != null && existingSession.isOpen()) {
                log.info("기존 WebSocket 세션 닫기 - 닉네임: {}", nickname);
                existingSession.close(CloseStatus.NORMAL);
            }

            // 세션 저장
            sessions.put(nickname, session);
            log.info("WebSocket 연결 성공 - 닉네임: {}", nickname);

        } catch (Exception e) {
            log.error("WebSocket 연결 실패: 예외 발생", e);
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("받은 메세지: {}", message.getPayload());
        try {
            //JSON 메시지 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> receivedMessage = objectMapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});

            String command = (String) receivedMessage.get("command");
            String data = (String) receivedMessage.get("data");

            if ("ping".equals(command)) {
                log.info("Ping command received with data: {}", data);

                //응답 메시지 전송
                String response = objectMapper.writeValueAsString(Map.of(
                        "response", "pong",
                        "originalData", data
                ));
                session.sendMessage(new TextMessage(response));
            } else {
                log.warn("Unknown command received: {}", command);
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);

            //오류 응답 전송
            String errorMessage = "{\"error\": \"Invalid message format\"}";
            session.sendMessage(new TextMessage(errorMessage));
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String nickname = extraNicknameFromSession(session);
        if (nickname != null) {
            sessions.remove(nickname);
            log.info("WebSocket 연결 종료 - 닉네임: {}", nickname);
        }
    }

    public void sendNotification(String nickname, String message) {
        WebSocketSession session = sessions.get(nickname);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("알림 전송 - 닉네임: {}, 메시지: {}", nickname, message);
            } catch (Exception e) {
                log.error("알림 전송 실패 - 닉네임: {}", nickname, e);
            }
        }
    }

    public void sendTaxNotification(String nickname, Long taxAmount) {
        WebSocketSession session = sessions.get(nickname);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                // JSON 메시지 생성
                String taxMessage = objectMapper.writeValueAsString(Map.of(
                        "type", "tax-alert",
                        "nickname", nickname,
                        "taxAmount", taxAmount
                ));
                session.sendMessage(new TextMessage(taxMessage)); // JSON 메시지 전송
                log.info("세금 알림 전송 - 닉네임: {}, 세금: {}", nickname, taxAmount);
            } catch (Exception e) {
                log.error("세금 알림 전송 실패 - 닉네임: {}", nickname, e);
            }
        } else {
            log.warn("세션이 열려 있지 않아 세금 알림 전송 실패 - 닉네임: {}", nickname);
        }
    }


    private String extraNicknameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
            log.info("Decoded Claims: {}", claims);

            // sub 필드에서 nickname 추출
            String sub = claims.get("sub", String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> subMap = objectMapper.readValue(sub, new TypeReference<Map<String, Object>>() {});

            return (String) subMap.get("nickname"); // nickname 반환
        } catch (Exception e) {
            log.error("JWT 검증 실패", e);
            return null;
        }
    }

    private String extraNicknameFromSession(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }

        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("token=")) {
            String token = query.substring(6);
            return extraNicknameFromToken(token);
        }
        return null;
    }

}
