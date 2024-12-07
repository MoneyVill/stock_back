package com.server.back.domain.notification.controller;

import com.server.back.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 테스트용으로 특정 사용자에게 알림 전송
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestParam String nickname, @RequestParam String message) {
        notificationService.sendNotification(nickname, message);
        return ResponseEntity.ok("Notification sent successfully.");
    }
}
