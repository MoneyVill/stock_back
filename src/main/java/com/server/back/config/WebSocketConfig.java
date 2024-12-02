package com.server.back.config;

import com.server.back.exception.DbDataWebSocketHandler;
import com.server.back.exception.TaxWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DbDataWebSocketHandler dbDataWebSocketHandler;
    private final TaxWebSocketHandler taxWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(dbDataWebSocketHandler, "/ws/db-data").setAllowedOrigins("*");
        registry.addHandler(taxWebSocketHandler, "/ws/tax-data").setAllowedOrigins("*");
    }
}
