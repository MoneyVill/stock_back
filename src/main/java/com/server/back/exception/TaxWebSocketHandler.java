package com.server.back.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.back.domain.rank.dto.TaxDetailsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class TaxWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    public TaxWebSocketHandler() {
        this.objectMapper = new ObjectMapper(); // ObjectMapper 초기화
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("Tax WebSocket 연결됨: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("Tax WebSocket 세션 닫힘: {}", session.getId());
    }

    /**
     * 세금 정보를 클라이언트에 전송
     */
    public void broadcastTaxDetails(List<TaxDetailsDto> taxDetailsList) {
        try {
            // 전송할 메시지를 JSON으로 변환
            String message = objectMapper.writeValueAsString(taxDetailsList);

            // 세금 정보를 로그에 출력
            log.info("전송할 세금 데이터:");
            taxDetailsList.forEach(detail ->
                    log.info("닉네임: {}, 세금 금액: {}",
                            detail.getNickname(), detail.getTaxAmount())
            );
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            }
            log.info("세금 정보가 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("세금 정보 전송 중 오류 발생: {}", e.getMessage());
        }
    }
}
