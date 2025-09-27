package com.waytoearth.websocket;

import com.waytoearth.service.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        try {
            String token = extractTokenFromRequest(request);

            if (token == null || !jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket 연결 거부 - 유효하지 않은 토큰: {}", request.getURI());
                return false;
            }

            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            attributes.put("userId", userId);
            attributes.put("token", token);

            log.info("WebSocket 인증 성공 - userId: {}", userId);
            return true;

        } catch (Exception e) {
            log.error("WebSocket 인증 실패", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake 실패", exception);
        }
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        URI uri = request.getURI();
        String query = uri.getQuery();

        if (query != null && query.contains("token=")) {
            try {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("token=")) {
                        return param.split("=")[1];
                    }
                }
            } catch (Exception e) {
                log.error("토큰 추출 실패", e);
            }
        }

        // Authorization 헤더에서 토큰 추출 시도
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}