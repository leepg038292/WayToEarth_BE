package com.waytoearth.config.scheduling;

import com.waytoearth.util.ChatRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ChatSchedulerConfig {

    private final ChatRateLimiter chatRateLimiter;

    /**
     * 1시간마다 Rate Limiter 정리
     */
    @Scheduled(fixedRate = 3600000) // 1시간 (3600000ms)
    public void cleanupRateLimiter() {
        try {
            chatRateLimiter.cleanup();
            log.debug("Rate limiter cleanup 완료");
        } catch (Exception e) {
            log.error("Rate limiter cleanup 중 오류 발생", e);
        }
    }
}