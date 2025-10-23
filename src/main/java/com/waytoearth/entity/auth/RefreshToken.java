package com.waytoearth.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Redis에 저장되는 리프레시 토큰 엔티티
 * - 액세스 토큰 갱신용
 * - 로그아웃 시 삭제하여 토큰 무효화
 * - 30일 후 자동 만료 (Redis TTL)
 */
@RedisHash(value = "refreshToken")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /**
     * Redis Key: "refreshToken:{userId}"
     * 사용자 ID를 Primary Key로 사용
     */
    @Id
    private Long userId;

    /**
     * 리프레시 토큰 값 (JWT)
     * - 인덱스 설정으로 토큰 값으로도 검색 가능
     */
    @Indexed
    private String token;

    /**
     * 발급 시간
     */
    private LocalDateTime issuedAt;

    /**
     * TTL (Time To Live) - Redis 자동 삭제 시간 (초)
     * 기본값: 2,592,000초 = 30일
     * Redis가 이 시간 후 자동으로 삭제
     */
    @TimeToLive
    @Builder.Default
    private Long expiration = 2592000L;

    /**
     * 리프레시 토큰 갱신 (재로그인 시)
     * @param newToken 새로운 리프레시 토큰
     */
    public void updateToken(String newToken) {
        this.token = newToken;
        this.issuedAt = LocalDateTime.now();
    }
}
