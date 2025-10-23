package com.waytoearth.service.auth;

import com.waytoearth.entity.enums.UserRole;
import com.waytoearth.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {


    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration:900000}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration:2592000000}") long refreshTokenExpiration) {
        log.info("JWT configuration initialized - access: {}ms, refresh: {}ms",
                accessTokenExpiration, refreshTokenExpiration);

        // 비밀키 최소 길이 검증 (HMAC-SHA256: 256비트 = 32바이트)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 bytes (256 bits) for HS256");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMs = accessTokenExpiration;
        this.refreshTokenExpirationMs = refreshTokenExpiration;
    }

    /**
     * JWT 토큰 생성 (Deprecated - generateAccessToken 사용 권장)
     * @deprecated use {@link #generateAccessToken(Long, UserRole)} instead
     */
    @Deprecated
    public String generateToken(Long userId, UserRole role) {
        // 기존 코드 호환성을 위해 accessToken으로 리다이렉트
        return generateAccessToken(userId, role);
    }

    /**
     * JWT 토큰 생성 (기본 역할: USER) - 하위 호환성 유지
     * @deprecated use {@link #generateAccessToken(Long, UserRole)} instead
     */
    @Deprecated
    public String generateToken(Long userId) {
        return generateAccessToken(userId, UserRole.USER);
    }

    /**
     * JWT 토큰에서 사용자 ID 추출
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰 파싱 에러: {}", e.getMessage());
            throw new UnauthorizedException("유효하지 않은 JWT 토큰입니다.");
        }
    }

    /**
     * JWT 토큰에서 역할(Role) 추출
     */
    public UserRole getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String roleStr = claims.get("role", String.class);
            return roleStr != null ? UserRole.valueOf(roleStr) : UserRole.USER;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰에서 role 추출 실패: {}", e.getMessage());
            return UserRole.USER; // 기본값 반환
        }
    }

    /**
     * JWT 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 액세스 토큰 생성 (15분)
     */
    public String generateAccessToken(Long userId, UserRole role) {
        Date expiryDate = new Date(System.currentTimeMillis() + accessTokenExpirationMs);
        return buildToken(userId, role, expiryDate);
    }

    /**
     * 리프레시 토큰 생성 (30일)
     * - role 정보 없음 (리프레시 전용)
     */
    public String generateRefreshToken(Long userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);
        return buildToken(userId, null, expiryDate);
    }

    /**
     * 토큰 만료까지 남은 시간 (초 단위)
     * - 블랙리스트 TTL 계산용
     */
    public Long getExpirationTime(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            long remainingTime = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            // 음수 방지 (이미 만료된 토큰)
            return Math.max(remainingTime, 0L);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] 토큰 만료 시간 추출 실패: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 토큰 만료까지 남은 일수
     * - 조건부 리프레시 토큰 재발급 판단용
     */
    public Long getRemainingDays(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Date expiration = claims.getExpiration();
            long remainingMs = expiration.getTime() - System.currentTimeMillis();

            // 밀리초 → 일수 변환
            return Math.max(remainingMs / (1000 * 60 * 60 * 24), 0L);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[JwtTokenProvider] 토큰 남은 일수 계산 실패: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 토큰 생성 공통 로직
     */
    private String buildToken(Long userId, UserRole role, Date expiryDate) {
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(secretKey);

        // role이 있을 때만 claim 추가 (액세스 토큰)
        if (role != null) {
            builder.claim("role", role.name());
        }

        return builder.compact();
    }
}