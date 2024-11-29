package com.server.back.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.back.domain.rank.dto.RankResDto;
import com.server.back.domain.rank.service.RankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class DbDataWebSocketHandler extends TextWebSocketHandler {

    private final RankService rankService;
    private final ObjectMapper objectMapper;

    public DbDataWebSocketHandler(RankService rankService) {
        this.rankService = rankService;
        this.objectMapper = new ObjectMapper(); // ObjectMapper 재사용을 위해 필드로 선언
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            log.info("WebSocket 연결됨: {} ", session.getId());

            AtomicReference<String> lastSentData = new AtomicReference<>(""); // 마지막으로 전송한 데이터를 저장

            // 초기 데이터 전송
            try {
                List<RankResDto>ranking=rankService.getRanking();
                String initialJsonData = convertToJson(ranking);
                session.sendMessage(new TextMessage(initialJsonData));
                lastSentData.set(initialJsonData);
            } catch (Exception e) {
                log.error("초기 데이터 전송 중 오류 발생: ", e);
            }

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (session.isOpen()) {
                            List<RankResDto>ranking=rankService.getRanking();
                            String jsonData = convertToJson(ranking);

                            // 데이터가 이전에 전송한 데이터와 다를 경우에만 전송
                            if (!jsonData.equals(lastSentData.get())) {
                                session.sendMessage(new TextMessage(jsonData));
                                lastSentData.set(jsonData); // 마지막으로 전송한 데이터 업데이트
                            }
                        } else {
                            log.info("WebSocket 세션 닫힘: {}", session.getId());
                            timer.cancel();
                        }
                    } catch (Exception e) {
                        log.error("WebSocket 데이터 전송 중 오류 발생: ", e);
                        timer.cancel();
                    }
                }
            }, 5000, 5000); // 1.5초마다 실행

        } catch (Exception e) {
            log.error("WebSocket 연결 처리 중 예외 발생: ", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket 세션 닫힘: {} (상태 코드: {}, 이유: {})", session.getId(), status.getCode(), status.getReason());
    }

    private String convertToJson(List<RankResDto> studentList) {
        try {
            return objectMapper.writeValueAsString(studentList);
        } catch (Exception e) {
            log.error("JSON 변환 실패: ", e);
            return "[]";
        }
    }
}