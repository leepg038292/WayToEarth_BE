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

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(crewChatWebSocketHandler, "/ws/crew/{crewId}/chat")
                .setAllowedOrigins(allowedOrigins.split(","))
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS();
    }
}