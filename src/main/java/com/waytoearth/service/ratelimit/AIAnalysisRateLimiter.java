package com.waytoearth.service.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * AI 분석 횟수 제한 서비스 (Redis 기반)
 * - 사용자별 일일 분석 횟수 제한
 * - 비용 관리 및 악용 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisRateLimiter {

    private static final String KEY_PREFIX = "ai_analysis_limit:";
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${openai.daily-limit-per-user}")
    private int dailyLimitPerUser;

    /**
     * 사용자가 오늘 AI 분석을 더 할 수 있는지 확인
     *
     * @param userId 사용자 ID
     * @return true: 가능, false: 제한 초과
     */
    public boolean canAnalyze(Long userId) {
        String key = buildKey(userId);
        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount == null) {
            return true;
        }

        int count = Integer.parseInt(currentCount);
        boolean canAnalyze = count < dailyLimitPerUser;

        log.debug("AI 분석 제한 확인 - 사용자: {}, 현재: {}/{}, 가능: {}",
                userId, count, dailyLimitPerUser, canAnalyze);

        return canAnalyze;
    }

    /**
     * 사용자의 오늘 분석 횟수 증가
     *
     * @param userId 사용자 ID
     * @return 증가 후 횟수
     */
    public int incrementCount(Long userId) {
        String key = buildKey(userId);
        Long newCount = redisTemplate.opsForValue().increment(key);

        if (newCount == null) {
            newCount = 1L;
        }

        // 첫 요청이면 자정까지 TTL 설정
        if (newCount == 1) {
            Duration ttl = calculateTimeUntilMidnight();
            redisTemplate.expire(key, ttl);
            log.info("AI 분석 카운터 생성 - 사용자: {}, TTL: {} 초", userId, ttl.getSeconds());
        }

        log.info("AI 분석 횟수 증가 - 사용자: {}, 현재: {}/{}", userId, newCount, dailyLimitPerUser);
        return newCount.intValue();
    }

    /**
     * 사용자의 오늘 남은 분석 횟수 조회
     *
     * @param userId 사용자 ID
     * @return 남은 횟수
     */
    public int getRemainingCount(Long userId) {
        String key = buildKey(userId);
        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount == null) {
            return dailyLimitPerUser;
        }

        int used = Integer.parseInt(currentCount);
        return Math.max(0, dailyLimitPerUser - used);
    }

    /**
     * 사용자의 오늘 사용 횟수 조회
     *
     * @param userId 사용자 ID
     * @return 사용 횟수
     */
    public int getUsedCount(Long userId) {
        String key = buildKey(userId);
        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount == null) {
            return 0;
        }

        return Integer.parseInt(currentCount);
    }

    /**
     * Redis 키 생성 (날짜 포함)
     */
    private String buildKey(Long userId) {
        LocalDate today = LocalDate.now(ZONE_ID);
        return KEY_PREFIX + userId + ":" + today;
    }

    /**
     * 자정까지 남은 시간 계산
     */
    private Duration calculateTimeUntilMidnight() {
        LocalDateTime now = LocalDateTime.now(ZONE_ID);
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();

        return Duration.between(now, midnight);
    }
}
