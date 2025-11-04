package com.waytoearth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 날씨 API Rate Limiter
 *
 * 사용자별 날씨 API 호출 빈도를 제한하여 서버 부하 방지 및 외부 API 비용 절감
 *
 * 제한 정책:
 * - 초당 최대 3회
 * - 분당 최대 10회
 */
@Component
@Slf4j
public class WeatherRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10; // 분당 최대 10회
    private static final int MAX_REQUESTS_PER_SECOND = 3;  // 초당 최대 3회

    // userId -> RequestRate 매핑
    private final Map<Long, RequestRate> userRates = new ConcurrentHashMap<>();

    /**
     * 사용자의 날씨 API 요청 가능 여부 확인 및 기록 (원자적 연산)
     *
     * @param userId 사용자 ID
     * @return 요청 가능 여부 (true: 가능, false: 제한 초과)
     */
    public boolean canRequest(Long userId) {
        RequestRate rate = userRates.computeIfAbsent(userId, k -> new RequestRate(userId));

        // synchronized로 check-then-act 패턴의 race condition 방지
        synchronized (rate) {
            if (rate.canRequest()) {
                rate.recordRequest();
                return true;
            }
            return false;
        }
    }

    /**
     * 주기적으로 오래된 데이터 정리 (메모리 누수 방지)
     *
     * 1시간 동안 요청이 없는 사용자 데이터 제거
     */
    public void cleanup() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        userRates.entrySet().removeIf(entry -> {
            RequestRate rate = entry.getValue();
            return rate.lastRequestTime.isBefore(oneHourAgo);
        });

        log.debug("Weather rate limiter cleanup completed. Active users: {}", userRates.size());
    }

    /**
     * 현재 활성 사용자 수 조회 (모니터링용)
     */
    public int getActiveUserCount() {
        return userRates.size();
    }

    /**
     * 사용자별 요청 빈도 추적
     */
    private static class RequestRate {
        private final Long userId;
        private int requestsThisMinute = 0;
        private int requestsThisSecond = 0;
        private LocalDateTime lastMinuteReset = LocalDateTime.now();
        private LocalDateTime lastSecondReset = LocalDateTime.now();
        private LocalDateTime lastRequestTime = LocalDateTime.now();

        RequestRate(Long userId) {
            this.userId = userId;
        }

        /**
         * 요청 가능 여부 확인
         */
        boolean canRequest() {
            LocalDateTime now = LocalDateTime.now();

            // 초 단위 리셋
            if (ChronoUnit.SECONDS.between(lastSecondReset, now) >= 1) {
                requestsThisSecond = 0;
                lastSecondReset = now;
            }

            // 분 단위 리셋
            if (ChronoUnit.MINUTES.between(lastMinuteReset, now) >= 1) {
                requestsThisMinute = 0;
                lastMinuteReset = now;
            }

            // 제한 확인
            if (requestsThisSecond >= MAX_REQUESTS_PER_SECOND) {
                log.warn("[날씨 API] 초당 요청 제한 초과 - userId: {}, 요청: {}회/초",
                         userId, requestsThisSecond);
                return false;
            }

            if (requestsThisMinute >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("[날씨 API] 분당 요청 제한 초과 - userId: {}, 요청: {}회/분",
                         userId, requestsThisMinute);
                return false;
            }

            return true;
        }

        /**
         * 요청 기록
         */
        void recordRequest() {
            requestsThisSecond++;
            requestsThisMinute++;
            lastRequestTime = LocalDateTime.now();
        }
    }
}
