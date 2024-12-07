package com.server.back.domain.notification.service;

public interface NotificationService {
    void sendNotification(String nickname, String message);
}