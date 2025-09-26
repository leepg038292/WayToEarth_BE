package com.waytoearth.config.websocket;

import com.waytoearth.websocket.CrewChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final CrewChatWebSocketHandler crewChatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(crewChatWebSocketHandler, "/ws/crew/{crewId}/chat")
                .setAllowedOrigins("*") // TODO: 프로덕션에서는 특정 도메인으로 제한
                .withSockJS();
    }
}