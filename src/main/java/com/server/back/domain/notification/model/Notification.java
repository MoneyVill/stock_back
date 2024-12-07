package com.server.back.domain.notification.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Notification {
    private String type;
    private String message;
}
