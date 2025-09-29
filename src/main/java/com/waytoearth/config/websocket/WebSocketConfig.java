package com.waytoearth.config.websocket;

import com.waytoearth.websocket.CrewChatWebSocketHandler;
import com.waytoearth.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CrewChatWebSocketHandler crewChatWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${app.cors.allowed-origins:https://api.waytoearth.cloud}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 순수 WebSocket 연결 (권장)
        registry.addHandler(crewChatWebSocketHandler, "/ws/crew/{crewId}/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns(allowedOrigins.split(","));

        // SockJS 연결 (백업용)
        registry.addHandler(crewChatWebSocketHandler, "/sockjs/crew/{crewId}/chat")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS()
                .setHeartbeatTime(25000)  // 25초마다 하트비트 전송
                .setDisconnectDelay(5000) // 5초 연결 끊김 감지
                .setSessionCookieNeeded(false);
    }

}