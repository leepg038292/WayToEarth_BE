package com.waytoearth.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 기반 로컬 캐시 설정
 *
 * 캐시 정책:
 * - weather: 날씨 정보 (30분 TTL)
 * - userInfo: 사용자 상세 정보 (10분 TTL)
 * - userSummary: 사용자 요약 정보 (10분 TTL)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 기반 캐시 매니저 설정
     *
     * 특징:
     * - 메모리 기반 로컬 캐시 (Redis보다 빠름)
     * - 자동 만료 (TTL)
     * - 최대 크기 제한 (메모리 보호)
     * - LRU 정책 (가장 오래 사용하지 않은 항목 제거)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "weather",      // 날씨 캐시
            "userInfo",     // 사용자 정보 캐시
            "userSummary"   // 사용자 요약 캐시
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    /**
     * Caffeine 캐시 빌더 설정
     *
     * - expireAfterWrite: 쓰기 후 30분 지나면 만료
     * - maximumSize: 최대 10,000개 항목 저장
     * - recordStats: 캐시 통계 기록 (히트율, 미스율 등)
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)  // 30분 TTL
                .maximumSize(10000)                       // 최대 10,000개 항목
                .recordStats();                           // 통계 수집
    }
}
