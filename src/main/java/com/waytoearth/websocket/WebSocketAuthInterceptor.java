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
        // 보안상 Authorization 헤더만 사용 (URL 쿼리 파라미터 사용 금지)
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // SEC-INFO 헤더에서도 토큰 확인 (SockJS fallback)
        String secInfoHeader = request.getHeaders().getFirst("Sec-WebSocket-Protocol");
        if (secInfoHeader != null && secInfoHeader.startsWith("Bearer.")) {
            return secInfoHeader.substring(7); // "Bearer." 제거
        }

        log.warn("WebSocket 연결에서 Authorization 헤더를 찾을 수 없음: {}",
                request.getURI().getPath());
        return null;
    }
}