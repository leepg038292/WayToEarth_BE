package com.waytoearth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatRateLimiter {

    private static final int MAX_MESSAGES_PER_MINUTE = 30; // 분당 최대 30개 메시지
    private static final int MAX_MESSAGES_PER_SECOND = 5;  // 초당 최대 5개 메시지

    // userId -> MessageRate 매핑
    private final Map<Long, MessageRate> userRates = new ConcurrentHashMap<>();

    /**
     * 사용자의 메시지 전송 가능 여부 확인 및 기록 (원자적 연산)
     */
    public boolean canSendMessage(Long userId) {
        MessageRate rate = userRates.computeIfAbsent(userId, k -> new MessageRate(userId));

        // synchronized로 check-then-act 패턴의 race condition 방지
        synchronized (rate) {
            if (rate.canSendMessage()) {
                rate.recordMessage();
                return true;
            }
            return false;
        }
    }

    /**
     * 사용자의 메시지 전송 기록 (사용하지 않음 - canSendMessage에서 통합 처리)
     * @deprecated canSendMessage 메서드에서 원자적으로 처리됨
     */
    @Deprecated
    public void recordMessage(Long userId) {
        // 더 이상 사용하지 않음 - canSendMessage에서 통합 처리
    }

    /**
     * 주기적으로 오래된 데이터 정리 (메모리 누수 방지)
     */
    public void cleanup() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        userRates.entrySet().removeIf(entry -> {
            MessageRate rate = entry.getValue();
            return rate.lastMessageTime.isBefore(oneHourAgo);
        });

        log.debug("Rate limiter cleanup completed. Active users: {}", userRates.size());
    }

    private static class MessageRate {
        private final Long userId;
        private int messagesThisMinute = 0;
        private int messagesThisSecond = 0;
        private LocalDateTime lastMinuteReset = LocalDateTime.now();
        private LocalDateTime lastSecondReset = LocalDateTime.now();
        private LocalDateTime lastMessageTime = LocalDateTime.now();

        MessageRate(Long userId) {
            this.userId = userId;
        }

        boolean canSendMessage() {
            LocalDateTime now = LocalDateTime.now();

            // 초 단위 리셋
            if (ChronoUnit.SECONDS.between(lastSecondReset, now) >= 1) {
                messagesThisSecond = 0;
                lastSecondReset = now;
            }

            // 분 단위 리셋
            if (ChronoUnit.MINUTES.between(lastMinuteReset, now) >= 1) {
                messagesThisMinute = 0;
                lastMinuteReset = now;
            }

            // 제한 확인
            if (messagesThisSecond >= MAX_MESSAGES_PER_SECOND) {
                log.warn("초당 메시지 제한 초과 - userId: {}, messages: {}",
                         userId, messagesThisSecond);
                return false;
            }

            if (messagesThisMinute >= MAX_MESSAGES_PER_MINUTE) {
                log.warn("분당 메시지 제한 초과 - userId: {}, messages: {}",
                         userId, messagesThisMinute);
                return false;
            }

            return true;
        }

        void recordMessage() {
            messagesThisSecond++;
            messagesThisMinute++;
            lastMessageTime = LocalDateTime.now();
        }
    }
}