package com.waytoearth.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketSessionManager {

    // 세션별 마지막 활동 시간 추적
    private final Map<String, LocalDateTime> sessionLastActivity = new ConcurrentHashMap<>();

    // 세션 타임아웃 (5분)
    private static final int SESSION_TIMEOUT_MINUTES = 5;

    /**
     * 세션 활동 시간 업데이트
     */
    public void updateSessionActivity(WebSocketSession session) {
        if (session != null && session.getId() != null) {
            sessionLastActivity.put(session.getId(), LocalDateTime.now());
        }
    }

    /**
     * 세션 제거
     */
    public void removeSession(WebSocketSession session) {
        if (session != null && session.getId() != null) {
            sessionLastActivity.remove(session.getId());
        }
    }

    /**
     * 세션이 타임아웃되었는지 확인
     */
    public boolean isSessionExpired(WebSocketSession session) {
        if (session == null || session.getId() == null) {
            return true;
        }

        LocalDateTime lastActivity = sessionLastActivity.get(session.getId());
        if (lastActivity == null) {
            return true;
        }

        return lastActivity.isBefore(LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES));
    }

    /**
     * 5분마다 비활성 세션 정리
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);

        int removedCount = 0;
        var iterator = sessionLastActivity.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isBefore(cutoffTime)) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("비활성 WebSocket 세션 {} 개 정리 완료", removedCount);
        }
    }

    /**
     * 현재 활성 세션 수
     */
    public int getActiveSessionCount() {
        return sessionLastActivity.size();
    }
}