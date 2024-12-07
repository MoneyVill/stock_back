package com.server.back.domain.notification.service;

import com.server.back.domain.notification.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public void sendNotification(String nickname, String message) {
        log.info("알림 서비스 - 사용자 ID: {}, 메시지: {}", nickname, message);
        webSocketHandler.sendNotification(nickname, message); // WebSocket으로 알림 전송
    }
}
