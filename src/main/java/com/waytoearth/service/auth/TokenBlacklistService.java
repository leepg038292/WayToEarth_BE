package com.waytoearth.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 토큰 블랙리스트 관리 서비스
 * - Redis StringRedisTemplate 사용 (단순 키-값)
 * - 로그아웃/회원탈퇴 시 액세스 토큰 무효화
 * - 토큰 존재 여부만 확인하면 되므로 엔티티 없이 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "tokenBlacklist:";

    /**
     * 토큰을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 액세스 토큰
     * @param expirationSeconds 토큰 만료까지 남은 시간 (초)
     */
    public void addToBlacklist(String token, Long expirationSeconds) {
        if (token == null || token.isEmpty()) {
            log.warn("[TokenBlacklist] 빈 토큰 블랙리스트 추가 시도 무시");
            return;
        }

        if (expirationSeconds <= 0) {
            log.warn("[TokenBlacklist] 만료된 토큰 블랙리스트 추가 불필요 - token: {}", maskToken(token));
            return;
        }

        String key = BLACKLIST_PREFIX + token;

        // Redis에 저장 (값은 "1", TTL은 토큰 만료 시간)
        redisTemplate.opsForValue().set(key, "1", expirationSeconds, TimeUnit.SECONDS);

        log.info("[TokenBlacklist] 토큰 블랙리스트 추가 - key: {}, ttl: {}s",
                maskToken(token), expirationSeconds);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @param token 확인할 액세스 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.warn("[TokenBlacklist] 블랙리스트 토큰 감지 - token: {}", maskToken(token));
        }

        return Boolean.TRUE.equals(exists);
    }

    /**
     * 토큰을 블랙리스트에서 제거 (테스트/관리용)
     * @param token 제거할 액세스 토큰
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);

        log.info("[TokenBlacklist] 토큰 블랙리스트 제거 - token: {}", maskToken(token));
    }

    /**
     * 토큰 마스킹 (로그 보안)
     * 예: "eyJhbGciOiJIUzI1NiJ9..." -> "eyJhbG...******"
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "******";
        }
        return token.substring(0, 7) + "..." + "******";
    }
}
