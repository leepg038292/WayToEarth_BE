package com.waytoearth.websocket;

import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.service.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CrewMemberRepository crewMemberRepository;

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

            // URL에서 crewId 추출
            Long crewId = extractCrewIdFromPath(request.getURI().getPath());

            if (crewId == null) {
                log.warn("WebSocket 연결 거부 - crewId를 찾을 수 없음: {}", request.getURI());
                return false;
            }

            // 크루 멤버인지 확인
            if (!crewMemberRepository.isUserMemberOfCrew(userId, crewId)) {
                log.warn("WebSocket 연결 거부 - 크루 멤버 아님: userId={}, crewId={}", userId, crewId);
                return false;
            }

            attributes.put("userId", userId);
            attributes.put("crewId", crewId);
            attributes.put("token", token);

            log.info("WebSocket 인증 성공 - userId: {}, crewId: {}", userId, crewId);
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

    /**
     * WebSocket URL에서 crewId 추출
     * 예: /ws/crew/123/chat → 123
     */
    private Long extractCrewIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("crew".equals(parts[i]) && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1]);
                }
            }
        } catch (NumberFormatException e) {
            log.error("crewId 파싱 실패: {}", path, e);
        }
        return null;
    }
}